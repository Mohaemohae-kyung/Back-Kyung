package kyung.kung_backend.domain.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@Schema(description = "결제 승인 확정 요청")
public class PaymentConfirmRequest {

    @Schema(
            description = "payments/prepare 응답으로 받은 주문 번호",
            example = "BOOKING-1-20260521100000-ab12cd34",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "주문 번호는 필수입니다.")
    private String orderId;

    @Schema(
            description = "PG가 결제 성공 후 내려주는 결제 키. Swagger 테스트에서는 매번 다른 테스트 문자열을 사용합니다.",
            example = "test-payment-key-001",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "PG 결제 키는 필수입니다.")
    private String paymentKey;

    @Schema(
            description = "payments/prepare 응답의 finalAmount와 동일한 금액",
            example = "66000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "결제 금액은 필수입니다.")
    @PositiveOrZero(message = "결제 금액은 0 이상이어야 합니다.")
    private BigDecimal amount;
}
