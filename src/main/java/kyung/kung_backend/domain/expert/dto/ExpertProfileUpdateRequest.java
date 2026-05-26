package kyung.kung_backend.domain.expert.dto;

import lombok.Getter;

@Getter
public class ExpertProfileUpdateRequest {

    private String displayName;
    private String introduction;
    private Double careerYears;
    private Long mainCategoryId;
    private Long mainLocationId;
}