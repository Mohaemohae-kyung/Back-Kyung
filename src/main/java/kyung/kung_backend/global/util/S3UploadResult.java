package kyung.kung_backend.global.util;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class S3UploadResult {

    private String fileKey;
    private String fileUrl;
}