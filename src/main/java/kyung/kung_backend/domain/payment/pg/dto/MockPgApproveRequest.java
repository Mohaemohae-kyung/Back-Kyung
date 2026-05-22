package kyung.kung_backend.domain.payment.pg.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@Schema(description = "Mock PG 결제 승인 요청")
public class MockPgApproveRequest {

    @Schema(
            description = "payments/prepare 응답으로 받은 주문 번호",
            example = "BOOKING-1-20260522100000-ab12cd34",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "주문 번호는 필수입니다.")
    private String orderId;

    @Schema(
            description = "payments/prepare 응답의 finalAmount와 동일한 금액",
            example = "100",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "결제 금액은 필수입니다.")
    @PositiveOrZero(message = "결제 금액은 0 이상이어야 합니다.")
    private BigDecimal amount;

    @Schema(
            description = "결제 수단. 테스트용 문자열이며 CARD, EASY_PAY 등을 입력할 수 있습니다.",
            example = "CARD",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "결제 수단은 필수입니다.")
    private String paymentMethod;
}
