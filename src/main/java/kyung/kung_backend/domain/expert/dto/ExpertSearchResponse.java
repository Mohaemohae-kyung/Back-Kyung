package kyung.kung_backend.domain.expert.dto;

import kyung.kung_backend.domain.expert.entity.ExpertProfile;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExpertSearchResponse {

    private Long expertProfileId;
    private String displayName;
    private String introduction;
    private Long careerYears;
    private String mainCategoryName;
    private String mainLocationName;
    private String verifiedYn;
    private String status;

    public static ExpertSearchResponse from(ExpertProfile expertProfile) {
        return new ExpertSearchResponse(
                expertProfile.getExpertProfileId(),
                expertProfile.getDisplayName(),
                expertProfile.getIntroduction(),
                expertProfile.getCareerYears(),
                expertProfile.getMainCategory() != null ? expertProfile.getMainCategory().getName() : null,
                expertProfile.getMainLocation() != null ? expertProfile.getMainLocation().getName() : null,
                expertProfile.getVerifiedYn(),
                expertProfile.getStatus()
        );
    }
}