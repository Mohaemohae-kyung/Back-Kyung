package kyung.kung_backend.domain.coupon.entity;

import jakarta.persistence.*;
import kyung.kung_backend.global.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "COUPONS",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_COUPONS_CODE", columnNames = "CODE")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "COUPONS_SEQ_GENERATOR",
        sequenceName = "COUPONS_SEQ",
        allocationSize = 1
)
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "COUPONS_SEQ_GENERATOR")
    @Column(name = "COUPON_ID", nullable = false)
    private Long couponId;

    @Column(name = "CODE", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "DISCOUNT_TYPE", nullable = false, length = 20)
    private String discountType;

    @Column(name = "DISCOUNT_AMOUNT", precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "DISCOUNT_RATE")
    private Long discountRate;

    @Column(name = "MIN_ORDER_AMOUNT", precision = 12, scale = 2)
    private BigDecimal minOrderAmount;

    @Column(name = "MAX_DISCOUNT_AMOUNT", precision = 12, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "START_AT", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "END_AT", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String DISCOUNT_TYPE_FIXED = "FIXED";
    public static final String DISCOUNT_TYPE_RATE = "RATE";

    public boolean isActive(LocalDateTime now) {
        return STATUS_ACTIVE.equals(this.status)
                && !this.startAt.isAfter(now)
                && !this.endAt.isBefore(now);

    }
}
