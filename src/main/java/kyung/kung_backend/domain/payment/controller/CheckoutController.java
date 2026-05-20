package kyung.kung_backend.domain.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kyung.kung_backend.domain.payment.dto.BookingCheckoutResponse;
import kyung.kung_backend.domain.payment.dto.ServiceRequestCheckoutResponse;
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
@Tag(name = "Checkout API", description = "마켓 예약과 견적 요청의 결제 화면 정보 조회 API")
public class CheckoutController {

    private final PaymentService paymentService;

    /*
     * 결제 화면 진입 API입니다.
     * 예약 정보, 서비스명, 고수명, 기본 금액, 최종 금액을 한 번에 내려줘서 프론트가 결제 페이지를 구성합니다.
     */
    @Operation(
            summary = "마켓 예약 결제 화면 조회",
            description = "마켓 예약의 결제 화면에 필요한 예약 정보와 금액을 조회합니다. " +
                    "BOOKINGS.STATUS가 PENDING_PAYMENT인 예약만 결제 가능하며, 이 API는 조회 전용이라 DB 값을 변경하지 않습니다."
    )
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

    /*
     * 견적 요청 기반 결제 화면 진입 API입니다.
     * ServiceRequest가 CHATTING 상태일 때 사용자가 요청 금액과 희망일시를 확인하고 결제로 넘어가는 데 사용합니다.
     */
    @Operation(
            summary = "견적 요청 결제 화면 조회",
            description = "견적 요청의 결제 화면에 필요한 고수 서비스 정보, 희망 일시, 요청 금액을 조회합니다. " +
                    "SERVICE_REQUESTS.STATUS가 CHATTING인 요청만 결제 가능하며, 이 API는 조회 전용이라 DB 값을 변경하지 않습니다."
    )
    @GetMapping("/service-requests/{requestId}")
    public ApiResponse<ServiceRequestCheckoutResponse> getServiceRequestCheckout(
            @AuthenticationPrincipal User user,
            @PathVariable Long requestId
    ) {
        return ApiResponse.onSuccess(
                SuccessCode.OK,
                paymentService.getServiceRequestCheckout(user, requestId)
        );
    }
}
