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

    /*
     * 현재 DB의 PAYMENTS 테이블에는 ORDER_ID가 NOT NULL로 존재합니다.
     * 주문 번호의 기준값은 Transaction.orderId이지만, 결제 시도 테이블에서도 빠르게 추적할 수 있도록
     * 같은 값을 중복 저장합니다.
     */
    @Column(name = "ORDER_ID", nullable = false, length = 100)
    private String orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_COUPON_ID")
    private UserCoupon userCoupon;

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

    @Column(name = "CANCELLED_AT")
    private LocalDateTime cancelledAt;

    @Column(name = "FAILED_REASON", length = 500)
    private String failedReason;

    public static final String STATUS_READY = "READY";
    public static final String STATUS_PAID = "PAID";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_REFUNDED = "REFUNDED";

    public static Payment createReady(
            Transaction transaction,
            User user,
            UserCoupon userCoupon,
            String paymentMethod,
            BigDecimal paymentAmount,
            String pgProvider
    ) {
        Payment payment = new Payment();

        payment.transaction = transaction;
        payment.user = user;
        payment.orderId = transaction.getOrderId();
        payment.userCoupon = userCoupon;
        payment.paymentMethod = paymentMethod;
        payment.paymentAmount = paymentAmount;
        payment.paymentStatus = STATUS_READY;
        payment.pgProvider = pgProvider;
        payment.pgPaymentKey =
                "TEST-" + transaction.getOrderId();
        payment.paidAt = null;
        payment.cancelledAt = null;
        payment.failedReason = null;

        return payment;
    }

    public boolean isReady() {
        return STATUS_READY.equals(this.paymentStatus);
    }

    public boolean isPaid() {
        return STATUS_PAID.equals(this.paymentStatus);
    }

    public void resetReady(
            Transaction transaction,
            UserCoupon userCoupon,
            String paymentMethod,
            BigDecimal paymentAmount,
            String pgProvider
    ) {
        this.transaction = transaction;
        this.orderId = transaction.getOrderId();
        this.userCoupon = userCoupon;
        this.paymentMethod = paymentMethod;
        this.paymentAmount = paymentAmount;
        this.paymentStatus = STATUS_READY;
        this.pgProvider = pgProvider;
        this.pgPaymentKey =
                "TEST-" + transaction.getOrderId();
        this.paidAt = null;
        this.cancelledAt = null;
        this.failedReason = null;
    }

    public void complete(
            String pgPaymentKey,
            LocalDateTime paidAt
    ) {
        this.paymentStatus = STATUS_PAID;
        this.pgPaymentKey = pgPaymentKey;
        this.paidAt = paidAt;
        this.failedReason = null;
    }

    public void fail(String failedReason) {
        this.paymentStatus = STATUS_FAILED;
        this.failedReason = failedReason;
    }

    public void cancel(String reason) {
        this.paymentStatus = STATUS_CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.failedReason = reason;
    }

    public void refund(String reason) {
        this.paymentStatus = STATUS_REFUNDED;
        this.cancelledAt = LocalDateTime.now();
        this.failedReason = reason;
    }
}
