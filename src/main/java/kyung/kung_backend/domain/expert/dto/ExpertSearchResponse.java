package kyung.kung_backend.domain.expert.dto;

import kyung.kung_backend.domain.expert.entity.ExpertProfile;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ExpertSearchResponse {

    private Long expertProfileId;

    private String displayName;
    private String introduction;
    private Double careerYears;

    private String mainLocationName;

    private List<String> categoryNames;

    private String verifiedYn;
    private String status;

    private String profileImageUrl;

    private String nickname;

    public static ExpertSearchResponse from(
            ExpertProfile expertProfile,
            List<String> categoryNames
    ) {

        return new ExpertSearchResponse(

                expertProfile.getExpertProfileId(),

                expertProfile.getDisplayName(),
                expertProfile.getIntroduction(),
                expertProfile.getCareerYears(),

                expertProfile.getMainLocation() != null
                        ? expertProfile.getMainLocation().getName()
                        : null,

                categoryNames,

                expertProfile.getVerifiedYn(),
                expertProfile.getStatus(),

                expertProfile.getUser() != null
                        ? expertProfile.getUser().getProfileImageUrl()
                        : null,

                expertProfile.getUser() != null
                        ? expertProfile.getUser().getNickname()
                        : null
        );
    }
}