package kyung.kung_backend.domain.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kyung.kung_backend.domain.payment.dto.PaymentPrepareRequest;
import kyung.kung_backend.domain.payment.dto.PaymentPrepareResponse;
import kyung.kung_backend.domain.payment.service.PaymentService;
import kyung.kung_backend.global.response.ApiResponse;
import kyung.kung_backend.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 결제 API 요청을 받는 Controller입니다.
 *
 * 호출 흐름:
 * - 사용자가 Swagger 또는 프론트엔드에서 /api/payments/prepare를 호출합니다.
 * - 이 Controller가 요청 JSON을 PaymentPrepareRequest로 변환해 받습니다.
 * - 실제 결제 준비 로직은 PaymentService.prepare()에 위임합니다.
 * - Service가 반환한 PaymentPrepareResponse를 공통 응답 ApiResponse로 감싸 반환합니다.
 *
 * Controller는 HTTP 요청/응답 형식을 담당하고,
 * 금액 계산, 주문번호 생성, DB 저장 같은 비즈니스 로직은 Service에 둡니다.
 */
@Tag(name = "Payment", description = "결제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 결제 준비 API입니다.
     *
     * 사용하는 곳:
     * - 프론트엔드에서 사용자가 결제 버튼을 누른 직후 호출합니다.
     * - Swagger에서는 Payment 그룹의 POST /api/payments/prepare 항목에서 직접 테스트할 수 있습니다.
     *
     * 하는 일:
     * - 예약/매칭 정보를 기준으로 서버에서 결제 금액을 확정합니다.
     * - 서버 주문번호(orderId)를 생성합니다.
     * - Transaction과 Payment를 READY 상태로 저장합니다.
     * - 이미 결제 준비된 예약이면 중복 생성하지 않고 409 응답을 반환합니다.
     *
     * 아직 하지 않는 일:
     * - PG사 결제 승인 호출
     * - 실제 결제 완료 처리
     * - 환불 처리
     */
    @Operation(
            summary = "결제 준비",
            description = "선택한 예약 건에 대해 결제 금액을 계산하고 서버 주문번호를 생성합니다. matchId를 함께 보내면 예약의 매칭 정보와 일치하는지 검증합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "결제 준비 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "bookingId 누락, 예약/매칭 불일치, 결제수단 오류, 결제 금액 오류")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 예약 또는 매칭")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 결제 준비가 완료된 예약")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "주문번호 생성 실패 등 서버 오류")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/prepare")
    public ApiResponse<PaymentPrepareResponse> prepare(@Valid @RequestBody PaymentPrepareRequest request) {
        PaymentPrepareResponse response = paymentService.prepare(request);
        return ApiResponse.onSuccess(SuccessCode.CREATED, response);
    }
}
