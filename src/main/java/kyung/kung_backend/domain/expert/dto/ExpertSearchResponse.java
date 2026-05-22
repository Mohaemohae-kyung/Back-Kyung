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

    private Long careerYears;

    private String mainCategoryName;
    private String mainLocationName;

    private String verifiedYn;
    private String status;

    public static ExpertSearchResponse from(
            kyung.kung_backend.domain.servicepost.entity.ExpertService expertService
    ) {

        ExpertProfile expertProfile = expertService.getExpertProfile();

        return new ExpertSearchResponse(
                expertService.getExpertServiceId(),
                expertProfile.getExpertProfileId(),

                expertProfile.getDisplayName(),
                expertProfile.getIntroduction(),

                expertService.getServiceTitle(),
                expertService.getServiceDescription(),

                expertService.getPrice(),

                expertProfile.getCareerYears(),

                expertProfile.getMainCategory() != null
                        ? expertProfile.getMainCategory().getName()
                        : null,

                expertProfile.getMainLocation() != null
                        ? expertProfile.getMainLocation().getName()
                        : null,

                expertProfile.getVerifiedYn(),
                expertProfile.getStatus()
        );
    }
}