package kyung.kung_backend.domain.expert.dto;

import kyung.kung_backend.domain.expert.entity.ExpertProfile;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExpertSearchResponse {

    private Long expertServiceId;
    private Long expertProfileId;

    private String displayName;
    private String introduction;

    private String serviceTitle;
    private String serviceDescription;

    private Integer price;

    private Double careerYears;

    // 서비스 기준 카테고리
    private String mainCategoryName;

    // 서비스 기준 지역
    private String mainLocationName;

    private String verifiedYn;
    private String status;

    private String profileImageUrl;

    public static ExpertSearchResponse from(
            kyung.kung_backend.domain.servicepost.entity.ExpertService expertService
    ) {

        ExpertProfile expertProfile =
                expertService.getExpertProfile();

        return new ExpertSearchResponse(

                expertService.getExpertServiceId(),

                expertProfile.getExpertProfileId(),

                expertProfile.getDisplayName(),

                expertProfile.getIntroduction(),

                expertService.getServiceTitle(),

                expertService.getServiceDescription(),

                expertService.getPrice(),

                expertProfile.getCareerYears(),

                // =========================
                // 서비스 카테고리 사용
                // =========================

                expertService.getCategory() != null
                        ? expertService.getCategory().getName()
                        : null,

                // =========================
                // 서비스 지역 사용
                // =========================

                expertService.getLocation() != null
                        ? expertService.getLocation().getName()
                        : null,

                expertProfile.getVerifiedYn(),

                expertProfile.getStatus(),

                expertProfile.getUser() != null
                        ? expertProfile.getUser().getProfileImageUrl()
                        : null
        );
    }
}