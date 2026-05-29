package kyung.kung_backend.domain.coupon.controller;

import kyung.kung_backend.domain.coupon.dto.AvailableCouponDto;
import kyung.kung_backend.domain.coupon.service.CouponService;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.response.ApiResponse;
import kyung.kung_backend.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @GetMapping("/api/coupons/usable")
    public ApiResponse<List<AvailableCouponDto>> getUsableCoupons(
            @AuthenticationPrincipal User user,
            @RequestParam String targetType,
            @RequestParam Long targetId
    ) {
        return ApiResponse.onSuccess(
                SuccessCode.OK,
                couponService.getUsableCoupons(user)
        );
    }
}