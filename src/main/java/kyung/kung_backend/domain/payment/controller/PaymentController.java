package kyung.kung_backend.domain.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import kyung.kung_backend.domain.payment.dto.PaymentCancelRequest;
import kyung.kung_backend.domain.payment.dto.PaymentConfirmRequest;
import kyung.kung_backend.domain.payment.dto.PaymentPrepareRequest;
import kyung.kung_backend.domain.payment.dto.PaymentPrepareResponse;
import kyung.kung_backend.domain.payment.dto.PaymentResponse;
import kyung.kung_backend.domain.payment.service.PaymentService;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.response.ApiResponse;
import kyung.kung_backend.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
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
                    "서버가 금액과 쿠폰을 다시 계산한 뒤 TRANSACTIONS와 PAYMENTS를 READY 상태로 생성하고 orderId를 발급합니다."
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
     * PG 결제 성공 후 호출하는 승인 API입니다.
     * 실제 운영에서는 이 API 안에서 PG 서버에 결제 승인 확인 요청을 한 뒤 예약/거래를 확정해야 합니다.
     */
    @Operation(
            summary = "결제 승인 처리",
            description = "PG 결제 성공 후 서버의 결제 상태를 확정하는 API입니다. " +
                    "prepare 응답의 orderId와 finalAmount를 기준으로 금액을 검증하고, 성공 시 PAYMENTS와 TRANSACTIONS를 PAID 상태로 변경합니다. " +
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
