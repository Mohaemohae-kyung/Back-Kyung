package kyung.kung_backend.domain.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class PaymentConfirmRequest {

    /*
     * payments/prepare에서 서버가 발급한 주문 번호입니다.
     * PG 결제 성공 후 프론트가 이 값을 다시 보내면 서버가 저장된 거래를 찾습니다.
     */
    @NotBlank(message = "주문 번호는 필수입니다.")
    private String orderId;

    /*
     * PG사가 결제 성공 후 내려주는 결제 키입니다.
     * 실제 운영에서는 이 값으로 PG 서버에 승인 확인 요청을 보냅니다.
     */
    @NotBlank(message = "PG 결제 키는 필수입니다.")
    private String paymentKey;

    /*
     * 프론트와 PG가 내려준 결제 금액입니다.
     * 서버에 저장된 최종 금액과 반드시 비교해서 금액 위변조를 막습니다.
     */
    @NotNull(message = "결제 금액은 필수입니다.")
    @PositiveOrZero(message = "결제 금액은 0 이상이어야 합니다.")
    private BigDecimal amount;
}
