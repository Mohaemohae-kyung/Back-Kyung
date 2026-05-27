package kyung.kung_backend.domain.expert.dto;

import kyung.kung_backend.domain.expert.entity.ExpertProfile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class ExpertDetailResponse {

    private Long expertServiceId;
    private Long expertProfileId;
    private Long ownerUserId;
    private String displayName;
    private String introduction;
    private String serviceTitle;
    private String serviceDescription;
    private Integer price;
    private Double careerYears;
    private String mainCategoryName;
    private String mainLocationName;
    private String verifiedYn;
    private String status;
    private String profileImageUrl;
    private String nickname;

    // 포트폴리오 프록시 웹뷰 URL 필드 추가
    private String portfolioWebViewUrl;

    private List<Long> expertServiceIds;

    public static ExpertDetailResponse from(
            kyung.kung_backend.domain.servicepost.entity.ExpertService expertService,
            List<Long> expertServiceIds,
            String portfolioWebViewUrl
    ) {

        ExpertProfile expertProfile = expertService.getExpertProfile();

        return new ExpertDetailResponse(
                expertService.getExpertServiceId(),
                expertProfile.getExpertProfileId(),
                expertProfile.getUser().getUserId(),
                expertProfile.getDisplayName(),
                expertProfile.getIntroduction(),
                expertService.getServiceTitle(),
                expertService.getServiceDescription(),
                expertService.getPrice(),
                expertProfile.getCareerYears(),
                expertService.getCategory() != null
                        ? expertService.getCategory().getName()
                        : null,
                expertService.getLocation() != null
                        ? expertService.getLocation().getName()
                        : null,
                expertProfile.getVerifiedYn(),
                expertProfile.getStatus(),
                expertProfile.getUser() != null
                        ? expertProfile.getUser().getProfileImageUrl()
                        : null,

                expertProfile.getUser() != null
                        ? expertProfile.getUser().getNickname()
                        : null,

                portfolioWebViewUrl,
                expertServiceIds
        );
    }
}