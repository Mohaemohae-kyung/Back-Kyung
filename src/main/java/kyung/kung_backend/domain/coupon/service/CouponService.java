package kyung.kung_backend.domain.coupon.service;

import kyung.kung_backend.domain.coupon.dto.AvailableCouponDto;
import kyung.kung_backend.domain.coupon.entity.UserCoupon;
import kyung.kung_backend.domain.coupon.repository.UserCouponRepository;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.domain.user.repository.UserRepository;
import kyung.kung_backend.global.exception.GeneralException;
import kyung.kung_backend.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {

    private final UserRepository userRepository;
    private final UserCouponRepository userCouponRepository;

    public List<AvailableCouponDto> getUsableCoupons(User loginUser) {
        if (loginUser == null || loginUser.getUserId() == null) {
            throw GeneralException.of(ErrorCode.UNAUTHORIZED);
        }

        User user = userRepository.findById(loginUser.getUserId())
                .orElseThrow(() -> GeneralException.of(ErrorCode.UNAUTHORIZED));

        LocalDateTime now = LocalDateTime.now();

        List<AvailableCouponDto> result = new ArrayList<>();

        if (user.isWelcomeCouponAvailable()) {
            result.add(
                    AvailableCouponDto.builder()
                            .userCouponId(-1L)
                            .name("웰컴 쿠폰")
                            .discountAmount(BigDecimal.valueOf(1000))
                            .expiredAt(null)
                            .build()
            );
        }

        List<UserCoupon> userCoupons =
                userCouponRepository.findAllByUserUserId(user.getUserId());

        result.addAll(
                userCoupons.stream()
                        .filter(userCoupon -> userCoupon.isUsable(now))
                        .map(AvailableCouponDto::from)
                        .toList()
        );

        return result;
    }
}