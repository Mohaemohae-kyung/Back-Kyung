package kyung.kung_backend.domain.payment.dto;

import kyung.kung_backend.domain.payment.entity.Payment;
import kyung.kung_backend.domain.transaction.entity.Transaction;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentResponse {

    private Long paymentId;
    private Long transactionId;
    private Long bookingId;
    private Long serviceRequestId;
    private String orderId;
    private String transactionType;
    private String paymentMethod;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private BigDecimal paymentAmount;
    private String transactionStatus;
    private String paymentStatus;
    private String pgProvider;
    private String pgPaymentKey;
    private LocalDateTime paidAt;
    private LocalDateTime cancelledAt;
    private String failedReason;
    private LocalDateTime createdAt;
    private String orderName;

    /*
     * 결제 상세/목록 조회용 응답입니다.
     * Transaction은 주문 단위 금액과 상태, Payment는 실제 결제 시도와 PG 응답 상태를 담습니다.
     */
    public static PaymentResponse from(Payment payment) {
        Transaction transaction = payment.getTransaction();

        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .transactionId(transaction.getTransactionId())
                .bookingId(transaction.getBooking() != null ? transaction.getBooking().getBookingId() : null)
                .serviceRequestId(transaction.getServiceRequest() != null
                        ? transaction.getServiceRequest().getRequestId()
                        : null)
                .orderId(payment.getOrderId())
                .transactionType(transaction.getTransactionType())
                .paymentMethod(payment.getPaymentMethod())
                .totalAmount(transaction.getTotalAmount())
                .discountAmount(transaction.getDiscountAmount())
                .finalAmount(transaction.getFinalAmount())
                .paymentAmount(payment.getPaymentAmount())
                .transactionStatus(transaction.getStatus())
                .paymentStatus(payment.getPaymentStatus())
                .pgProvider(payment.getPgProvider())
                .pgPaymentKey(payment.getPgPaymentKey())
                .paidAt(payment.getPaidAt())
                .cancelledAt(payment.getCancelledAt())
                .failedReason(payment.getFailedReason())
                .createdAt(payment.getCreatedAt())
                .orderName(
                        transaction.getServiceRequest() != null
                                ? transaction.getServiceRequest().getTitle()
                                : null
                )
                .build();
    }
}
