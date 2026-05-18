package kyung.kung_backend.domain.coupon.entity;

import jakarta.persistence.*;
import kyung.kung_backend.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "USER_COUPONS")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "USER_COUPONS_SEQ_GENERATOR",
        sequenceName = "USER_COUPONS_SEQ",
        allocationSize = 1
)
public class UserCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "USER_COUPONS_SEQ_GENERATOR")
    @Column(name = "USER_COUPON_ID", nullable = false)
    private Long userCouponId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "COUPON_ID", nullable = false)
    private Coupon coupon;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;

    @Column(name = "ISSUED_AT", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "USED_AT")
    private LocalDateTime usedAt;

    @Column(name = "EXPIRED_AT")
    private LocalDateTime expiredAt;
}