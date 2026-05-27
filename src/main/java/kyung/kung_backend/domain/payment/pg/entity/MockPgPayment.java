package kyung.kung_backend.domain.payment.pg.entity;

import jakarta.persistence.*;
import kyung.kung_backend.global.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "MOCK_PG_PAYMENTS",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_MOCK_PG_ORDER_ID", columnNames = "ORDER_ID"),
                @UniqueConstraint(name = "UK_MOCK_PG_PAYMENT_KEY", columnNames = "PAYMENT_KEY")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "MOCK_PG_PAYMENTS_SEQ_GENERATOR",
        sequenceName = "MOCK_PG_PAYMENTS_SEQ",
        allocationSize = 1
)
public class MockPgPayment extends BaseEntity {

    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MOCK_PG_PAYMENTS_SEQ_GENERATOR")
    @Column(name = "MOCK_PG_PAYMENT_ID", nullable = false)
    private Long mockPgPaymentId;

    @Column(name = "ORDER_ID", nullable = false, length = 100)
    private String orderId;

    @Column(name = "PAYMENT_KEY", nullable = false, length = 255)
    private String paymentKey;

    @Column(name = "AMOUNT", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "PAYMENT_METHOD", nullable = false, length = 30)
    private String paymentMethod;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;

    @Column(name = "APPROVED_AT", nullable = false)
    private LocalDateTime approvedAt;

    @Column(name = "CANCELLED_AT")
    private LocalDateTime cancelledAt;

    /*
     * 실제 PG가 결제 성공 후 paymentKey를 내려주는 역할을 단순화한 테스트용 factory입니다.
     * orderId와 amount는 payments/prepare 응답값을 그대로 받아 저장하고,
     * payments/confirm에서는 이 저장된 승인 기록이 있는지 다시 검증합니다.
     */
    public static MockPgPayment approve(
            String orderId,
            BigDecimal amount,
            String paymentMethod,
            LocalDateTime approvedAt
    ) {
        MockPgPayment mockPgPayment = new MockPgPayment();

        mockPgPayment.orderId = orderId;
        mockPgPayment.paymentKey = createPaymentKey();
        mockPgPayment.amount = amount;
        mockPgPayment.paymentMethod = paymentMethod;
        mockPgPayment.status = STATUS_APPROVED;
        mockPgPayment.approvedAt = approvedAt;
        mockPgPayment.cancelledAt = null;

        return mockPgPayment;
    }

    public boolean isApproved() {
        return STATUS_APPROVED.equals(this.status);
    }

    public void cancel(LocalDateTime cancelledAt) {
        this.status = STATUS_CANCELLED;
        this.cancelledAt = cancelledAt;
    }

    private static String createPaymentKey() {
        return "mock_pg_" + UUID.randomUUID().toString().replace("-", "");
    }
}
