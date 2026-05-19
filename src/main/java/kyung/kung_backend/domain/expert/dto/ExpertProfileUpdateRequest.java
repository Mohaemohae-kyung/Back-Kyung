package kyung.kung_backend.domain.expert.dto;

import lombok.Getter;

@Getter
public class ExpertProfileUpdateRequest {

    private String displayName;
    private String introduction;
    private Long careerYears;
    private Long mainCategoryId;
    private Long mainLocationId;
}