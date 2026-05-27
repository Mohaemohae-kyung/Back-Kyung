package kyung.kung_backend.domain.servicepost.dto;

import kyung.kung_backend.domain.servicepost.entity.ExpertService;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExpertServiceResponse {

    private Long expertServiceId;
    private Long expertProfileId;
    private String expertName;
    private Long categoryId;
    private String categoryName;
    private String status;

    public static ExpertServiceResponse from(ExpertService expertService) {
        return ExpertServiceResponse.builder()
                .expertServiceId(expertService.getExpertServiceId())
                .expertProfileId(expertService.getExpertProfile().getExpertProfileId())
                .expertName(expertService.getExpertProfile().getDisplayName())
                .categoryId(expertService.getCategory().getCategoryId())
                .categoryName(expertService.getCategory().getName())
                .build();
    }
}