package kyung.kung_backend.domain.expert.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class ExpertProfileCreateRequest {

    private String displayName;
    private String introduction;
    private Double careerYears;

    private Long mainCategoryId;
    private List<Long> categoryIds;

    private Long mainLocationId;
    private String externalPortfolioUrl;
}