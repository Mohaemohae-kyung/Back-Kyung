package kyung.kung_backend.domain.favorite.dto;

import kyung.kung_backend.domain.expert.entity.ExpertProfile;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FavoriteExpertResponse {

    private Long expertProfileId;
    private String displayName;
    private Double careerYears;
    private String mainCategoryName;
    private boolean favorite;

    public static FavoriteExpertResponse from(ExpertProfile expertProfile) {
        return FavoriteExpertResponse.builder()
                .expertProfileId(expertProfile.getExpertProfileId())
                .displayName(expertProfile.getDisplayName())
                .careerYears(expertProfile.getCareerYears())
                .mainCategoryName(
                        expertProfile.getMainCategory() != null
                                ? expertProfile.getMainCategory().getName()
                                : null
                )
                .favorite(true)
                .build();
    }
}