package kyung.kung_backend.domain.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kyung.kung_backend.domain.payment.entity.Payment;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 결제 준비 API 응답 DTO입니다.
 *
 * 사용 흐름:
 * 1. PaymentService.prepare()가 Transaction과 Payment를 생성한 뒤 이 DTO를 만듭니다.
 * 2. PaymentController.prepare()가 ApiResponse로 감싸 클라이언트에 반환합니다.
 * 3. 클라이언트는 응답의 orderId와 amount를 사용해 이후 PG 결제창 또는 결제 승인 요청을 진행합니다.
 *
 * 현재는 prepare 스켈레톤이므로 PG사 clientKey, checkoutUrl 같은 값은 포함하지 않습니다.
 * PG 연동 방식이 확정되면 이 응답 DTO에 필요한 필드를 추가하면 됩니다.
 */
@Getter
@Schema(description = "결제 준비 응답")
public class PaymentPrepareResponse {

    @Schema(description = "서버에 생성된 결제 ID", example = "1")
    private final Long paymentId;

    @Schema(description = "서버에 생성된 거래 ID", example = "1")
    private final Long transactionId;

    @Schema(description = "PG 승인 단계에서 사용할 서버 주문번호", example = "ORDER-20260518153000123-A1B2C3D4")
    private final String orderId;

    @Schema(description = "결제 준비 금액", example = "50000.00")
    private final BigDecimal amount;

    @Schema(description = "결제 수단", example = "CARD")
    private final String paymentMethod;

    @Schema(description = "결제 상태. prepare 직후에는 READY 상태입니다.", example = "READY")
    private final String paymentStatus;

    private PaymentPrepareResponse(
            Long paymentId,
            Long transactionId,
            String orderId,
            BigDecimal amount,
            String paymentMethod,
            String paymentStatus
    ) {
        this.paymentId = paymentId;
        this.transactionId = transactionId;
        this.orderId = orderId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
    }

    /**
     * Entity를 API 응답 형태로 변환합니다.
     *
     * Controller가 Entity를 직접 반환하지 않도록 Service에서 이 메서드를 호출합니다.
     * 이렇게 분리하면 DB 내부 구조가 바뀌어도 API 응답 구조를 안정적으로 유지할 수 있습니다.
     */
    public static PaymentPrepareResponse from(Payment payment) {
        return new PaymentPrepareResponse(
                payment.getPaymentId(),
                payment.getTransaction().getTransactionId(),
                payment.getOrderId(),
                payment.getPaymentAmount(),
                payment.getPaymentMethod(),
                payment.getPaymentStatus()
        );
    }
}
