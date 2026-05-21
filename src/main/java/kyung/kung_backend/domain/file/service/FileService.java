package kyung.kung_backend.domain.file.service;

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
                .storedName(savedFile.getStoredName())
                .fileUrl(savedFile.getFileUrl())
                .build();
    }

    public Resource downloadFile(String storedName) {
        FileUpload fileUpload = getFileInfo(storedName);

        if ("DELETED".equals(fileUpload.getStatus())) {
            throw new IllegalArgumentException("삭제된 파일입니다.");
        }

        return storageProvider.loadAsResource(fileUpload.getStoredName());
    }

    public FileUpload getFileInfo(String storedName) {
        return fileUploadRepository.findByStoredName(storedName)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다."));
    }

    @Transactional
    public void deleteFile(User user, String storedName) {
        FileUpload fileUpload = getFileInfo(storedName);

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