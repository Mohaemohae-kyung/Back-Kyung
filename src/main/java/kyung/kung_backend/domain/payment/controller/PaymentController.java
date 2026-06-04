package kyung.kung_backend.domain.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kyung.kung_backend.domain.payment.dto.PaymentCancelRequest;
import kyung.kung_backend.domain.payment.dto.PaymentResponse;
import kyung.kung_backend.domain.payment.dto.ServiceRequestPaymentRequestCreateRequest;
import kyung.kung_backend.domain.payment.service.PaymentService;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.response.ApiResponse;
import kyung.kung_backend.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
@Tag(name = "Payment API", description = "결제 준비, 승인 확정, 취소/환불, 결제 내역 조회 API")
public class PaymentController {

    private final PaymentService paymentService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String NODE_SERVER_URL = "http://100.104.59.126:4000";
    private final String NODE_CRYPTO_SERVER_URL = NODE_SERVER_URL + "/api/crypto";


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
            summary = "결제 준비 (E2E 순수 프록시)",
            description = "E2E 암호문을 전혀 열어보지 않고 결제 전담 서버(Node.js)로 중계합니다."
    )
    @PostMapping("/prepare")
    public ResponseEntity<java.util.Map> preparePaymentProxy(
            @AuthenticationPrincipal User user,
            @RequestBody java.util.Map<String, Object> request
    ) {
        System.out.println("=== [Spring Proxy] prepare request (PURE PROXY) ===");
        
        // Node.js에서 유저 식별을 할 수 있도록 평문 헤더/바디 추가
        request.put("userId", user.getUserId());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<java.util.Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            // Node.js 결제 전담 서버로 순수 릴레이
            java.util.Map response = restTemplate.postForObject(
                    NODE_SERVER_URL + "/api/payments/prepare",
                    entity,
                    java.util.Map.class
            );
            return ResponseEntity.ok(response);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(e.getResponseBodyAs(java.util.Map.class));
        }
    }

    @Operation(
            summary = "결제 승인 처리 (E2E 순수 프록시)",
            description = "E2E 승인 암호문을 전혀 열어보지 않고 결제 전담 서버(Node.js)로 중계합니다."
    )
    @PostMapping("/confirm")
    public ResponseEntity<java.util.Map> confirmPaymentProxy(
            @RequestBody java.util.Map<String, Object> request
    ) {
        System.out.println("=== [Spring Proxy] confirm request (PURE PROXY) ===");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<java.util.Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            // Node.js 결제 전담 서버로 순수 릴레이
            java.util.Map response = restTemplate.postForObject(
                    NODE_SERVER_URL + "/api/payments/confirm",
                    entity,
                    java.util.Map.class
            );
            return ResponseEntity.ok(response);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(e.getResponseBodyAs(java.util.Map.class));
        }
    }

    @Operation(
            summary = "결제 비밀번호 설정 (E2E 순수 프록시)",
            description = "E2E 암호문을 전혀 열어보지 않고 결제 전담 서버(Node.js)로 중계합니다."
    )
    @PostMapping("/password/setup")
    public ResponseEntity<java.util.Map> setupPasswordProxy(
            @AuthenticationPrincipal User user,
            @RequestBody java.util.Map<String, Object> request
    ) {
        System.out.println("=== [Spring Proxy] password setup request (PURE PROXY) ===");
        
        request.put("userId", user.getUserId());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<java.util.Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            // Node.js 서버로 순수 릴레이
            java.util.Map response = restTemplate.postForObject(
                    NODE_SERVER_URL + "/api/payments/password/setup",
                    entity,
                    java.util.Map.class
            );
            return ResponseEntity.ok(response);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(e.getResponseBodyAs(java.util.Map.class));
        }
    }

    @Operation(
            summary = "결제 비밀번호 검증 (E2E 순수 프록시)",
            description = "E2E 암호문을 전혀 열어보지 않고 결제 전담 서버(Node.js)로 중계합니다."
    )
    @PostMapping("/password/verify")
    public ResponseEntity<java.util.Map> verifyPasswordProxy(
            @AuthenticationPrincipal User user,
            @RequestBody java.util.Map<String, Object> request
    ) {
        System.out.println("=== [Spring Proxy] password verify request (PURE PROXY) ===");

        request.put("userId", user.getUserId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<java.util.Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            java.util.Map response = restTemplate.postForObject(
                    NODE_SERVER_URL + "/api/payments/password/verify",
                    entity,
                    java.util.Map.class
            );
            return ResponseEntity.ok(response);
        } catch (HttpStatusCodeException e) {
            String nodeMessage = "결제 비밀번호가 일치하지 않습니다.";

            try {
                // 자동 변환 오류를 방지하기 위해 문자열로 추출 후 ObjectMapper를 사용하여 안전하게 파싱
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.Map<String, Object> nodeError = mapper.readValue(
                        e.getResponseBodyAsString(),
                        new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>(){}
                );

                if (nodeError != null && nodeError.get("message") != null) {
                    nodeMessage = String.valueOf(nodeError.get("message"));
                }
            } catch (Exception parseEx) {
                System.out.println("Node.js 응답 파싱 실패: " + parseEx.getMessage());
            }

            java.util.Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("isSuccess", false);
            errorResponse.put("code", "PAYMENT_ERROR");
            errorResponse.put("message", nodeMessage);
            errorResponse.put("result", null);

            // 프론트엔드의 글로벌 401 인터셉터와 충돌하지 않도록 400 상태 코드로 우회하여 전달
            HttpStatus status = (e.getStatusCode().value() == 401) ? HttpStatus.BAD_REQUEST : (HttpStatus) e.getStatusCode();

            return ResponseEntity.status(status)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }
    }

    @Operation(
            summary = "결제 비밀번호 변경 (E2E 순수 프록시)",
            description = "E2E 암호문을 전혀 열어보지 않고 결제 전담 서버(Node.js)로 중계합니다."
    )
    @PostMapping("/password/change")
    public ResponseEntity<java.util.Map> changePasswordProxy(
            @AuthenticationPrincipal User user,
            @RequestBody java.util.Map<String, Object> request
    ) {
        System.out.println("=== [Spring Proxy] password change request (PURE PROXY) ===");

        request.put("userId", user.getUserId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<java.util.Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            java.util.Map response = restTemplate.postForObject(
                    NODE_SERVER_URL + "/api/payments/password/change",
                    entity,
                    java.util.Map.class
            );
            return ResponseEntity.ok(response);
        } catch (HttpStatusCodeException e) {
            String nodeMessage = "결제 비밀번호가 일치하지 않습니다.";

            try {
                // 자동 변환 오류를 방지하기 위해 문자열로 추출 후 ObjectMapper를 사용하여 안전하게 파싱
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.Map<String, Object> nodeError = mapper.readValue(
                        e.getResponseBodyAsString(),
                        new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>(){}
                );

                if (nodeError != null && nodeError.get("message") != null) {
                    nodeMessage = String.valueOf(nodeError.get("message"));
                }
            } catch (Exception parseEx) {
                System.out.println("Node.js 응답 파싱 실패: " + parseEx.getMessage());
            }

            java.util.Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("isSuccess", false);
            errorResponse.put("code", "PAYMENT_ERROR");
            errorResponse.put("message", nodeMessage);
            errorResponse.put("result", null);

            // 프론트엔드의 글로벌 401 인터셉터와 충돌하지 않도록 400 상태 코드로 우회하여 전달
            HttpStatus status = (e.getStatusCode().value() == 401) ? HttpStatus.BAD_REQUEST : (HttpStatus) e.getStatusCode();

            return ResponseEntity.status(status)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }
    }

    @Operation(
            summary = "견적 요청 결제 요청 메시지 생성",
            description = "고수/전문가가 고객에게 결제 요청 메시지를 보냅니다. 실제 결제 비밀번호 검증과 Toss 결제 준비는 고객이 결제할 때 수행합니다."
    )
    @PostMapping("/service-requests/{requestId}/request")
    public ApiResponse<PaymentResponse> createServiceRequestPaymentRequest(
            @AuthenticationPrincipal User user,
            @PathVariable Long requestId,
            @Valid @RequestBody ServiceRequestPaymentRequestCreateRequest request
    ) {
        return ApiResponse.onSuccess(
                SuccessCode.CREATED,
                paymentService.createServiceRequestPaymentRequest(user, requestId, request)
        );
    }

    // =========================================================================
    // Node.js 결제 서버 전용 Internal API (백엔드는 E2E 데이터를 모르고 이것만 제공)
    // =========================================================================
    @GetMapping("/internal/target-info")
    public ResponseEntity<java.util.Map<String, Object>> getTargetInfo(
            @RequestParam String targetType,
            @RequestParam Long targetId,
            @RequestParam Long userId,
            @RequestParam(required = false) Long userCouponId
    ) {
        java.util.Map<String, Object> info = paymentService.getTargetInfoForNode(targetType, targetId, userId, userCouponId);
        return ResponseEntity.ok(info);
    }

    @PostMapping("/internal/complete")
    public ResponseEntity<Void> completePaymentInternal(
            @RequestBody java.util.Map<String, Object> request
    ) {
        paymentService.completePaymentFromNode(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/internal/ready")
    public ResponseEntity<java.util.Map<String, Object>> createReadyPayment(@RequestBody java.util.Map<String, Object> request) {
        // Node.js 서버가 결제 준비를 마친 후, Spring Boot에 READY 결제와 채팅 메시지를 생성하도록 요청함.
        Long paymentId = paymentService.prepareReadyPaymentProxy(request);
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("paymentId", paymentId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/internal/password")
    public ResponseEntity<Void> updatePaymentPasswordInternal(
            @RequestBody java.util.Map<String, Object> request
    ) {
        paymentService.updatePaymentPasswordHash(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/internal/password/{userId}")
    public ResponseEntity<java.util.Map<String, Object>> getPaymentPasswordHash(@PathVariable Long userId) {
        String hash = paymentService.getPaymentPasswordHash(userId);
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        if (hash != null) {
            response.put("hash", hash);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/internal/suspend/{userId}")
    public ResponseEntity<Void> suspendUserInternal(@PathVariable Long userId) {
        paymentService.suspendUser(userId);
        return ResponseEntity.ok().build();
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
