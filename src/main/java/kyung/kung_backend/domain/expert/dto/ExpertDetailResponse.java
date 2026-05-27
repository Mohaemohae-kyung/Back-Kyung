package kyung.kung_backend.domain.expert.dto;

import kyung.kung_backend.domain.expert.entity.ExpertProfile;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ExpertDetailResponse {

    private Long expertProfileId;
    private Long ownerUserId;

    private String displayName;
    private String introduction;
    private Double careerYears;

    private String mainCategoryName;
    private String mainLocationName;

    private List<String> categoryNames;
    private List<Long> expertServiceIds;

    private String verifiedYn;
    private String status;
    private String profileImageUrl;

    // 포트폴리오 프록시 웹뷰 URL 필드 추가
    private String portfolioWebViewUrl;

    public static ExpertDetailResponse from(
            ExpertProfile expertProfile,
            List<String> categoryNames,
            List<Long> expertServiceIds,
            String portfolioWebViewUrl
    ) {

        return new ExpertDetailResponse(
                expertProfile.getExpertProfileId(),

                expertProfile.getUser() != null
                        ? expertProfile.getUser().getUserId()
                        : null,

                expertProfile.getDisplayName(),
                expertProfile.getIntroduction(),
                expertProfile.getCareerYears(),

                expertProfile.getMainCategory() != null
                        ? expertProfile.getMainCategory().getName()
                        : null,

                expertProfile.getMainLocation() != null
                        ? expertProfile.getMainLocation().getName()
                        : null,

                categoryNames,
                expertServiceIds,

                expertProfile.getVerifiedYn(),
                expertProfile.getStatus(),

                expertProfile.getUser() != null
                        ? expertProfile.getUser().getProfileImageUrl()
                        : null,

                portfolioWebViewUrl
        );
    }
}