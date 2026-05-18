package kyung.kung_backend.domain.payment.exception;

import kyung.kung_backend.global.response.BaseCode;
import kyung.kung_backend.global.response.ReasonDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 결제 도메인에서 사용하는 에러 코드입니다.
 *
 * 왜 별도로 두는지:
 * - global.response.ErrorCode는 COMMON_400, COMMON_404 같은 공통 코드만 가지고 있습니다.
 * - 결제 API를 개발하다 보면 "예약 없음", "이미 결제 준비됨", "금액 없음"처럼 결제 도메인에서만 의미 있는 실패가 생깁니다.
 * - 이런 실패를 PAYMENT_xxx 코드로 분리하면 Swagger/Postman에서 어떤 단계의 어떤 실패인지 더 빨리 확인할 수 있습니다.
 *
 * 사용 흐름:
 * - PaymentService.prepare()에서 검증 실패 시 PaymentException과 함께 이 enum 값을 던집니다.
 * - GlobalExceptionHandler가 PaymentException을 받아 httpStatus/code/message를 ApiResponse로 변환합니다.
 *
 * 이후 확장:
 * - confirm API에서는 PAYMENT_NOT_FOUND, PAYMENT_AMOUNT_MISMATCH, PAYMENT_ALREADY_CONFIRMED 같은 코드를 여기에 추가합니다.
 * - refund API에서는 PAYMENT_REFUND_ALREADY_REQUESTED, PAYMENT_REFUND_FAILED 같은 코드를 추가합니다.
 */
@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements BaseCode {

    PREPARE_TARGET_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "PAYMENT_400_001",
            "결제 준비를 위해 bookingId가 필요합니다."
    ),
    BOOKING_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "PAYMENT_404_001",
            "존재하지 않는 예약입니다."
    ),
    MATCH_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "PAYMENT_404_002",
            "존재하지 않는 매칭입니다."
    ),
    BOOKING_MATCH_MISMATCH(
            HttpStatus.BAD_REQUEST,
            "PAYMENT_400_002",
            "예약 정보와 매칭 정보가 일치하지 않습니다."
    ),
    INVALID_PAYMENT_AMOUNT(
            HttpStatus.BAD_REQUEST,
            "PAYMENT_400_003",
            "결제 가능한 견적 금액이 없습니다."
    ),
    INVALID_PAYMENT_METHOD(
            HttpStatus.BAD_REQUEST,
            "PAYMENT_400_004",
            "지원하지 않는 결제수단입니다."
    ),
    PAYMENT_ALREADY_PREPARED(
            HttpStatus.CONFLICT,
            "PAYMENT_409_001",
            "이미 결제 준비가 완료된 예약입니다."
    ),
    ORDER_ID_GENERATION_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "PAYMENT_500_001",
            "주문번호 생성에 실패했습니다."
    );

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ReasonDto getReason() {
        return ReasonDto.builder()
                .httpStatus(httpStatus)
                .code(code)
                .message(message)
                .build();
    }
}
