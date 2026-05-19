package kyung.kung_backend.domain.file.service;

import io.swagger.v3.oas.annotations.Operation;
import kyung.kung_backend.domain.file.dto.FileUploadResponse;
import kyung.kung_backend.domain.file.entity.FileUpload;
import kyung.kung_backend.domain.file.repository.FileUploadRepository;
import kyung.kung_backend.domain.file.storage.StorageProvider;
import kyung.kung_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileService {

    private final FileUploadRepository fileUploadRepository;
    private final StorageProvider storageProvider;

    @Operation(
            summary = "물리 파일 저장 및 데이터베이스 메타데이터 기록",
            description = "주입된 StorageProvider 인터페이스를 통해 파일을 저장소 인프라에 업로드하고, 데이터베이스에 소유자 및 도메인 식별 정보를 매핑하여 저장합니다."
    )
    @Transactional
    public FileUploadResponse uploadFile(User user, MultipartFile file, String domain) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        String originalName = file.getOriginalFilename();
        String storedName = UUID.randomUUID().toString() + "_" + originalName;

        String fileUrl = storageProvider.store(file, storedName);

        FileUpload fileUpload = FileUpload.builder()
                .uploader(user)
                .targetType(domain)
                .originalName(originalName)
                .storedName(storedName)
                .fileUrl(fileUrl)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .build();

        FileUpload savedFile = fileUploadRepository.save(fileUpload);

        return FileUploadResponse.builder()
                .fileId(savedFile.getFileId())
                .fileUrl(savedFile.getFileUrl())
                .build();
    }

    @Operation(
            summary = "파일 다운로드 리소스 조회",
            description = "고유 식별 번호로 파일 메타데이터를 검색하고 활성화 상태를 확인한 후, 보관소 인프라로부터 실제 데이터 리소스를 로드합니다."
    )
    public Resource downloadFile(Long fileId) {
        FileUpload fileUpload = getFileInfo(fileId);

        if ("DELETED".equals(fileUpload.getStatus())) {
            throw new IllegalArgumentException("삭제된 파일입니다.");
        }

        return storageProvider.loadAsResource(fileUpload.getStoredName());
    }

    @Operation(
            summary = "파일 정보 조회",
            description = "데이터베이스에서 특정 파일 엔티티의 식별 메타데이터 정보를 단건 조회합니다."
    )
    public FileUpload getFileInfo(Long fileId) {
        return fileUploadRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다."));
    }

    @Operation(
            summary = "파일 소유권 검증 및 파기",
            description = "해당 파일을 업로드한 주체와 일치하는지 권한을 검증한 뒤, 저장소 인프라에서 물리 파일을 파기하고 엔티티의 상태를 변경합니다."
    )
    @Transactional
    public void deleteFile(User user, Long fileId) {
        FileUpload fileUpload = getFileInfo(fileId);

        if (!fileUpload.getUploader().getUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("파일을 삭제할 권한이 없습니다.");
        }

        if ("DELETED".equals(fileUpload.getStatus())) {
            throw new IllegalArgumentException("이미 삭제된 파일입니다.");
        }

        storageProvider.delete(fileUpload.getStoredName());

        fileUpload.delete();
    }
}