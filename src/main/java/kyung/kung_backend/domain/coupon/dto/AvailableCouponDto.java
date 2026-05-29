package kyung.kung_backend.domain.coupon.dto;

import kyung.kung_backend.domain.coupon.entity.UserCoupon;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class AvailableCouponDto {

    private Long userCouponId;
    private String name;
    private BigDecimal discountAmount;
    private LocalDateTime expiredAt;

    public static AvailableCouponDto from(UserCoupon userCoupon) {
        return AvailableCouponDto.builder()
                .userCouponId(userCoupon.getUserCouponId())
                .name(userCoupon.getCoupon().getName())
                .discountAmount(userCoupon.getCoupon().getDiscountAmount())
                .expiredAt(userCoupon.getExpiredAt())
                .build();
    }
}