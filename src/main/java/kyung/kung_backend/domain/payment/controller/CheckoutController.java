package kyung.kung_backend.domain.payment.controller;

import kyung.kung_backend.domain.payment.dto.BookingCheckoutResponse;
import kyung.kung_backend.domain.payment.service.PaymentService;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.response.ApiResponse;
import kyung.kung_backend.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/checkout")
public class CheckoutController {

    private final PaymentService paymentService;

    /*
     * 결제 화면 진입 API입니다.
     * 예약 정보, 서비스명, 고수명, 기본 금액, 최종 금액을 한 번에 내려줘서 프론트가 결제 페이지를 구성합니다.
     */
    @GetMapping("/bookings/{bookingId}")
    public ApiResponse<BookingCheckoutResponse> getBookingCheckout(
            @AuthenticationPrincipal User user,
            @PathVariable Long bookingId
    ) {
        return ApiResponse.onSuccess(
                SuccessCode.OK,
                paymentService.getBookingCheckout(user, bookingId)
        );
    }
}
