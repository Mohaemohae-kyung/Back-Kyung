package kyung.kung_backend.domain.payment.pg.dto;

import kyung.kung_backend.domain.payment.pg.entity.MockPgPayment;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class MockPgApproveResponse {

    private Long mockPgPaymentId;
    private String orderId;
    private String paymentKey;
    private BigDecimal amount;
    private String paymentMethod;
    private String status;
    private LocalDateTime approvedAt;

    public static MockPgApproveResponse from(MockPgPayment mockPgPayment) {
        return MockPgApproveResponse.builder()
                .mockPgPaymentId(mockPgPayment.getMockPgPaymentId())
                .orderId(mockPgPayment.getOrderId())
                .paymentKey(mockPgPayment.getPaymentKey())
                .amount(mockPgPayment.getAmount())
                .paymentMethod(mockPgPayment.getPaymentMethod())
                .status(mockPgPayment.getStatus())
                .approvedAt(mockPgPayment.getApprovedAt())
                .build();
    }
}
