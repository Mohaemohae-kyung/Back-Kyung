package kyung.kung_backend.domain.expert.dto;

import kyung.kung_backend.domain.expert.entity.ExpertProfile;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ExpertDetailResponse {

    private Long expertProfileId;
    private String displayName;
    private String introduction;
    private Long careerYears;
    private String mainCategoryName;
    private String mainLocationName;
    private String verifiedYn;
    private String status;

    // 견적 요청 생성 시 프론트가 선택해서 넘길 수 있는 expertServiceId 목록
    private List<Long> expertServiceIds;

    public static ExpertDetailResponse from(
            ExpertProfile expertProfile,
            List<Long> expertServiceIds
    ) {
        return new ExpertDetailResponse(
                expertProfile.getExpertProfileId(),
                expertProfile.getDisplayName(),
                expertProfile.getIntroduction(),
                expertProfile.getCareerYears(),
                expertProfile.getMainCategory() != null ? expertProfile.getMainCategory().getName() : null,
                expertProfile.getMainLocation() != null ? expertProfile.getMainLocation().getName() : null,
                expertProfile.getVerifiedYn(),
                expertProfile.getStatus(),
                expertServiceIds
        );
    }
}