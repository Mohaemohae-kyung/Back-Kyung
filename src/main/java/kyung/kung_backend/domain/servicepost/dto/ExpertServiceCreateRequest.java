package kyung.kung_backend.domain.servicepost.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ExpertServiceCreateRequest {

    private Long categoryId;

    private String serviceTitle;

    private String serviceDescription;

    private Integer price;
}