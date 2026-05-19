package kyung.kung_backend.domain.file.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileUploadResponse {
    private Long fileId;
    private String fileUrl;
}