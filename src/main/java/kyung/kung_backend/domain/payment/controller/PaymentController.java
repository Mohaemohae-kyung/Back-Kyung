package kyung.kung_backend.domain.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kyung.kung_backend.domain.payment.dto.*;
import kyung.kung_backend.domain.payment.service.PaymentService;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.response.ApiResponse;
import kyung.kung_backend.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
@Tag(name = "Payment API", description = "결제 준비, 승인 확정, 취소/환불, 결제 내역 조회 API")
public class PaymentController {

    private final PaymentService paymentService;

    /*
     * PG 결제창을 띄우기 직전에 호출하는 API입니다.
     * 서버가 예약/견적 금액과 쿠폰을 기준으로 최종 금액을 계산하고, PG에 넘길 orderId를 발급합니다.
     */
    @Operation(
            summary = "결제 준비 및 주문번호 발급",
            description = "PG 결제창을 열기 직전에 호출하는 API입니다. " +
                    "마켓 예약 결제는 targetType=BOOKING과 bookingId를 사용하고, 견적 요청 결제는 targetType=SERVICE_REQUEST와 requestId를 사용합니다. " +
                    "쿠폰은 마켓 예약 결제에서만 사용할 수 있으며, 견적 요청 결제에서 userCouponId를 보내면 거절됩니다. " +
                    "서버가 금액과 쿠폰을 다시 계산한 뒤 TRANSACTIONS와 PAYMENTS를 READY 상태로 생성하고 orderId를 발급합니다. " +
                    "응답의 orderId와 finalAmount로 토스페이먼츠 결제창을 호출합니다."
    )
    @PostMapping("/prepare")
    public ResponseEntity<ApiResponse<PaymentPrepareResponse>> preparePayment(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody PaymentPrepareRequest request
    ) {
        PaymentPrepareResponse response = paymentService.preparePayment(user, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.onSuccess(SuccessCode.CREATED, response));
    }

    /*
     * PG 결제 성공 후 결제 서버(Payment Server)에서 동기화를 위해 호출하는 API입니다.
     * 결제 서버가 승인을 마친 후 orderId와 paymentKey를 넘기면 즉시 결제 상태를 확정합니다.
     */
    @Operation(
            summary = "결제 승인 처리 (결제 서버 연동)",
            description = "결제 서버(Payment Server)가 토스페이먼츠 승인을 마친 후 호출하는 API입니다. " +
                    "해당 orderId를 가진 결제 내역을 즉시 PAID 상태로 변경합니다. " +
                    "마켓 예약 결제는 BOOKINGS를 CONFIRMED로, 견적 요청 결제는 SERVICE_REQUESTS를 COMPLETED로 변경합니다."
    )
    @PostMapping("/confirm")
    public ApiResponse<PaymentResponse> confirmPayment(
            @Valid @RequestBody PaymentConfirmRequest request
    ) {
        return ApiResponse.onSuccess(SuccessCode.OK, paymentService.confirmPayment(request));
    }

    /*
     * 결제 취소 또는 환불 API입니다.
     * READY 결제는 서버 상태만 취소하고, PAID 결제는 환불 상태로 바꾸며 예약은 취소 처리합니다.
     */
    @Operation(
            summary = "결제 취소 또는 환불",
            description = "결제 준비 상태 또는 결제 완료 상태의 결제를 취소/환불 처리합니다. " +
                    "READY 결제는 PAYMENTS와 TRANSACTIONS를 CANCELLED로 변경하고, PAID 결제는 REFUNDED로 변경합니다. " +
                    "마켓 예약 결제는 연결된 BOOKINGS도 CANCELLED로 변경합니다."
    )
    @PostMapping("/{paymentId}/cancel")
    public ApiResponse<PaymentResponse> cancelPayment(
            @AuthenticationPrincipal User user,
            @PathVariable Long paymentId,
            @Valid @RequestBody PaymentCancelRequest request
    ) {
        return ApiResponse.onSuccess(
                SuccessCode.OK,
                paymentService.cancelPayment(user, paymentId, request)
        );
    }

    /*
     * 결제 상세 조회입니다.
     * 결제 완료 화면, 마이페이지 결제 내역 상세, CS 확인 화면에서 사용합니다.
     */
    @Operation(
            summary = "결제 상세 조회",
            description = "paymentId로 결제 상세 정보를 조회합니다. " +
                    "주문번호, 결제 금액, 거래 상태, 결제 상태, PG 결제 키, 결제/취소 시각을 확인할 때 사용합니다."
    )
    @GetMapping("/{paymentId}")
    public ApiResponse<PaymentResponse> getPaymentDetail(
            @AuthenticationPrincipal User user,
            @PathVariable Long paymentId
    ) {
        return ApiResponse.onSuccess(SuccessCode.OK, paymentService.getPaymentDetail(user, paymentId));
    }

    /*
     * 로그인 사용자의 결제 내역 목록입니다.
     * 예약 결제와 견적 요청 결제를 결제 화면에서 같이 보여줄 수 있도록 Payment 기준으로 내려줍니다.
     */
    @Operation(
            summary = "내 결제 목록 조회",
            description = "로그인한 사용자의 결제 이력을 최신순으로 조회합니다. " +
                    "마켓 예약 결제와 견적 요청 결제를 모두 Payment 기준으로 내려줍니다."
    )
    @GetMapping("/me")
    public ApiResponse<List<PaymentResponse>> getMyPayments(
            @AuthenticationPrincipal User user
    ) {
        return ApiResponse.onSuccess(SuccessCode.OK, paymentService.getMyPayments(user));
    }
}
