package kyung.kung_backend.domain.servicepost.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ExpertServiceCreateRequest {

    private Long categoryId;

    // =========================
    // 서비스별 활동 지역 추가
    // =========================

    private Long locationId;

    private String serviceTitle;

    private String serviceDescription;

    private Integer price = 0;
}