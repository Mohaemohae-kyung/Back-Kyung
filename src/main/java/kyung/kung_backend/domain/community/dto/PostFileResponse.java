package kyung.kung_backend.domain.community.dto;

import kyung.kung_backend.domain.file.entity.FileUpload;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostFileResponse {

    private Long fileId;
    private String originalName;
    private String storedName;
    private String fileUrl;
    private String contentType;
    private Long fileSize;

    public static PostFileResponse from(FileUpload file) {
        return PostFileResponse.builder()
                .fileId(file.getFileId())
                .originalName(file.getOriginalName())
                .storedName(file.getStoredName())
                .fileUrl(file.getFileUrl())
                .contentType(file.getContentType())
                .fileSize(file.getFileSize())
                .build();
    }
}