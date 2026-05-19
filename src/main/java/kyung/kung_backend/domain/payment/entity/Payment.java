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
}