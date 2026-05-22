package kyung.kung_backend.domain.file.service;

import kyung.kung_backend.domain.file.dto.FileUploadResponse;
import kyung.kung_backend.domain.file.entity.FileUpload;
import kyung.kung_backend.domain.file.repository.FileUploadRepository;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.util.S3Service;
import kyung.kung_backend.global.util.S3UploadResult;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileService {

    private final FileUploadRepository fileUploadRepository;
    private final S3Service s3Service;

    @Transactional
    public FileUploadResponse uploadFile(User user, MultipartFile file, String domain) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        String originalName = file.getOriginalFilename();

        S3UploadResult uploadResult = s3Service.uploadFile(file, domain);

        FileUpload fileUpload = FileUpload.builder()
                .uploader(user)
                .targetType(domain)
                .originalName(originalName)
                // storedName에는 이제 로컬 파일명이 아니라 S3 object key를 저장합니다.
                .storedName(uploadResult.getFileKey())
                .fileUrl(uploadResult.getFileUrl())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .build();

        FileUpload savedFile = fileUploadRepository.save(fileUpload);

        return FileUploadResponse.builder()
                .storedName(savedFile.getStoredName())
                .fileUrl(savedFile.getFileUrl())
                .build();
    }

    public Resource downloadFile(String fileKey) {
        FileUpload fileUpload = getFileInfo(fileKey);

        if ("DELETED".equals(fileUpload.getStatus())) {
            throw new IllegalArgumentException("삭제된 파일입니다.");
        }

        return s3Service.downloadFile(fileUpload.getStoredName());
    }

    public FileUpload getFileInfo(String fileKey) {
        return fileUploadRepository.findByStoredName(fileKey)
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

        // storedName에 저장된 S3 object key를 이용해 S3 객체 삭제
        s3Service.deleteFile(fileUpload.getStoredName());

        fileUpload.delete();
    }
}