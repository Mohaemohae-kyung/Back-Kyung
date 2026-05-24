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

    // =========================
    // 게시글 작성자 USER ID
    // =========================
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

    // 견적 요청 생성 시 사용할 서비스 ID 목록
    private List<Long> expertServiceIds;

    public static ExpertDetailResponse from(
            kyung.kung_backend.domain.servicepost.entity.ExpertService expertService,
            List<Long> expertServiceIds
    ) {

        ExpertProfile expertProfile =
                expertService.getExpertProfile();

        return new ExpertDetailResponse(

                expertService.getExpertServiceId(),

                expertProfile.getExpertProfileId(),

                // =========================
                // 작성자 USER ID
                // =========================
                expertProfile.getUser().getUserId(),

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
                        : null,

                expertServiceIds
        );
    }
}