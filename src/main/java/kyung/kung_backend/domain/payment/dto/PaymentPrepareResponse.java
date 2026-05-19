package kyung.kung_backend.domain.payment.dto;

import kyung.kung_backend.domain.payment.entity.Payment;
import kyung.kung_backend.domain.transaction.entity.Transaction;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PaymentPrepareResponse {

    private Long paymentId;
    private Long transactionId;
    private String orderId;
    private String orderName;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private String paymentStatus;

    /*
     * 결제 위젯을 띄우기 전에 프론트가 필요한 값을 내려줍니다.
     * orderId와 finalAmount는 PG 결제 요청에 사용되고, confirm API에서 다시 검증됩니다.
     */
    public static PaymentPrepareResponse of(
            Payment payment,
            Transaction transaction,
            String orderName
    ) {
        return PaymentPrepareResponse.builder()
                .paymentId(payment.getPaymentId())
                .transactionId(transaction.getTransactionId())
                .orderId(transaction.getOrderId())
                .orderName(orderName)
                .totalAmount(transaction.getTotalAmount())
                .discountAmount(transaction.getDiscountAmount())
                .finalAmount(transaction.getFinalAmount())
                .paymentStatus(payment.getPaymentStatus())
                .build();
    }
}
