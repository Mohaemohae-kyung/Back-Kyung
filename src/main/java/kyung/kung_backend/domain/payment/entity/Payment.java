package kyung.kung_backend.domain.payment.entity;

import jakarta.persistence.*;
import kyung.kung_backend.domain.coupon.entity.UserCoupon;
import kyung.kung_backend.domain.transaction.entity.Transaction;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "PAYMENTS",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_PAYMENTS_ORDER_ID", columnNames = "ORDER_ID"),
                @UniqueConstraint(name = "UK_PAYMENTS_PG_KEY", columnNames = "PG_PAYMENT_KEY")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "PAYMENTS_SEQ_GENERATOR",
        sequenceName = "PAYMENTS_SEQ",
        allocationSize = 1
)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PAYMENTS_SEQ_GENERATOR")
    @Column(name = "PAYMENT_ID", nullable = false)
    private Long paymentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "TRANSACTION_ID", nullable = false)
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_COUPON_ID")
    private UserCoupon userCoupon;

    @Column(name = "ORDER_ID", nullable = false, unique = true, length = 100)
    private String orderId;

    @Column(name = "PAYMENT_METHOD", nullable = false, length = 30)
    private String paymentMethod;

    @Column(name = "PAYMENT_AMOUNT", nullable = false, precision = 12, scale = 2)
    private BigDecimal paymentAmount;

    @Column(name = "PAYMENT_STATUS", nullable = false, length = 20)
    private String paymentStatus;

    @Column(name = "PG_PROVIDER", length = 50)
    private String pgProvider;

    @Column(name = "PG_PAYMENT_KEY", unique = true, length = 255)
    private String pgPaymentKey;

    @Column(name = "PAID_AT")
    private LocalDateTime paidAt;

    /**
     * 결제 준비 단계의 Payment 객체를 생성합니다.
     *
     * 호출 위치:
     * - PaymentService.prepare()에서 Transaction을 먼저 저장한 뒤 이 메서드를 호출합니다.
     *
     * 사용 목적:
     * - Controller나 Service에서 setter로 필드를 하나씩 열어두지 않고,
     *   "결제 준비 상태의 Payment는 어떤 값이 필요하다"는 규칙을 Entity 안에 모아둡니다.
     *
     * 현재 prepare 단계에서는 PG 결제가 아직 승인되지 않았으므로
     * pgProvider, pgPaymentKey, paidAt은 비워둡니다.
     * 이후 confirm API에서 PG 승인 성공 시 해당 필드를 채우는 메서드를 별도로 추가하면 됩니다.
     */
    public static Payment prepare(
            Transaction transaction,
            User user,
            String orderId,
            String paymentMethod,
            BigDecimal paymentAmount,
            String paymentStatus
    ) {
        Payment payment = new Payment();
        payment.transaction = transaction;
        payment.user = user;
        payment.orderId = orderId;
        payment.paymentMethod = paymentMethod;
        payment.paymentAmount = paymentAmount;
        payment.paymentStatus = paymentStatus;
        return payment;
    }
}
