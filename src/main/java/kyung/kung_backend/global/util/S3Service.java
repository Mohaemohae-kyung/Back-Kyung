package kyung.kung_backend.global.util;

import kyung.kung_backend.global.config.S3Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    // 파일 업로드 후 S3 URL 반환
    public String uploadFile(
            MultipartFile file,
            String directory
    ) {
        validateFile(file);

        String key = createFileKey(file.getOriginalFilename(), directory);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3Properties.getS3().getBucket())
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            return getFileUrl(key);

        } catch (IOException e) {
            throw new IllegalArgumentException("S3 파일 업로드 중 오류가 발생했습니다.", e);
        }
    }

    // 파일 삭제
    public void deleteFile(String fileKey) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(s3Properties.getS3().getBucket())
                .key(fileKey)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }

    // S3 객체 URL 생성
    public String getFileUrl(String key) {
        GetUrlRequest getUrlRequest = GetUrlRequest.builder()
                .bucket(s3Properties.getS3().getBucket())
                .key(key)
                .build();

        URL url = s3Client.utilities().getUrl(getUrlRequest);
        return url.toString();
    }

    // S3 저장 key 생성
    private String createFileKey(
            String originalFilename,
            String directory
    ) {
        String extension = extractExtension(originalFilename);
        String datePath = LocalDate.now().toString();

        return s3Properties.getS3().getUploadPath()
                + "/"
                + directory
                + "/"
                + datePath
                + "/"
                + UUID.randomUUID()
                + extension;
    }

    // 확장자 추출
    private String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }

        return originalFilename.substring(originalFilename.lastIndexOf("."));
    }

    // 파일 검증
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }
    }
}
