package kyung.kung_backend.domain.expert.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExpertProfileImageUploadResponse {

    private Long expertProfileId;

    private String originalName;

    private String storedName;

    private String expertProfileImageUrl;

    private String contentType;

    private Long fileSize;
}
