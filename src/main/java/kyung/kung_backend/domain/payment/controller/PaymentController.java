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

import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
@Tag(name = "Payment API", description = "결제 준비, 승인 확정, 취소/환불, 결제 내역 조회 API")
public class PaymentController {

    private final PaymentService paymentService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String NODE_CRYPTO_SERVER_URL = "http://100.104.59.126:4000/api/crypto";


    @Operation(
            summary = "E2E 결제용 RSA 공개키 발급",
            description = "Node.js 결제/암호 서버에서 발급한 RSA 공개키를 받아 프론트엔드에 전달합니다."
    )
    @GetMapping("/public-key")
    public ResponseEntity<java.util.Map> getPublicKey() {
        java.util.Map response = restTemplate.getForObject(NODE_CRYPTO_SERVER_URL + "/public-key", java.util.Map.class);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "결제 준비 (E2E Proxy)",
            description = "클라이언트의 E2E 암호문을 Node.js 서버로 릴레이합니다."
    )
    @PostMapping("/prepare")
    public ResponseEntity<E2ePayloadResponse> preparePaymentProxy(
            @RequestBody java.util.Map<String, String> request
    ) {
        System.out.println("=== [Spring Proxy] prepare request ===");
        System.out.println("encryptedAesKey: " + request.get("encryptedAesKey"));
        System.out.println("iv: " + request.get("iv"));
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<java.util.Map<String, String>> entity = new HttpEntity<>(request, headers);

        E2ePayloadResponse response = restTemplate.postForObject(
                NODE_CRYPTO_SERVER_URL + "/prepare",
                entity,
                E2ePayloadResponse.class
        );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "결제 승인 (E2E Proxy)",
            description = "클라이언트의 E2E 승인 암호문을 Node.js 서버로 릴레이합니다."
    )
    @PostMapping("/confirm")
    public ResponseEntity<E2ePayloadResponse> confirmPaymentProxy(
            @RequestBody java.util.Map<String, String> request
    ) {
        System.out.println("=== [Spring Proxy] confirm request ===");
        System.out.println("encryptedAesKey: " + request.get("encryptedAesKey"));
        System.out.println("iv: " + request.get("iv"));
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<java.util.Map<String, String>> entity = new HttpEntity<>(request, headers);

        E2ePayloadResponse response = restTemplate.postForObject(
                NODE_CRYPTO_SERVER_URL + "/confirm",
                entity,
                E2ePayloadResponse.class
        );

        return ResponseEntity.ok(response);
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
