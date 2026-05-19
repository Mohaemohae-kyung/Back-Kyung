package kyung.kung_backend.domain.request.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequestUpdateRequest {

    @Size(max = 200, message = "제목은 200자 이하로 입력해주세요.")
    private String title;

    private String content;

    private BigDecimal budget;

    private LocalDateTime preferredDate;
}