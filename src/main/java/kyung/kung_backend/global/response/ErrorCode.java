package kyung.kung_backend.global.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode implements BaseCode {

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_400", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_401", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_403", "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_404", "요청한 리소스를 찾을 수 없습니다."),
    CONFLICT(HttpStatus.CONFLICT, "COMMON_409", "이미 존재하거나 충돌이 발생한 요청입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "USER_409_1", "이미 사용 중인 이메일입니다."),
    DUPLICATE_PHONE(HttpStatus.CONFLICT, "USER_409_2", "이미 사용 중인 전화번호입니다."),
    INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "AUTH_401_1", "아이디 또는 비밀번호가 올바르지 않습니다."),
    DELETED_USER(HttpStatus.UNAUTHORIZED, "AUTH_401_2", "탈퇴한 회원입니다."),
    PAYMENT_PREPARE_TARGET_REQUIRED(HttpStatus.BAD_REQUEST, "PAYMENT_400_1", "결제 준비를 위해 bookingId가 필요합니다."),
    PAYMENT_BOOKING_MATCH_MISMATCH(HttpStatus.BAD_REQUEST, "PAYMENT_400_2", "예약 정보와 매칭 정보가 일치하지 않습니다."),
    PAYMENT_INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "PAYMENT_400_3", "결제 가능한 견적 금액이 없습니다."),
    PAYMENT_INVALID_METHOD(HttpStatus.BAD_REQUEST, "PAYMENT_400_4", "지원하지 않는 결제수단입니다."),
    PAYMENT_BOOKING_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_404_1", "존재하지 않는 예약입니다."),
    PAYMENT_MATCH_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_404_2", "존재하지 않는 매칭입니다."),
    PAYMENT_ALREADY_PREPARED(HttpStatus.CONFLICT, "PAYMENT_409_1", "이미 결제 준비가 완료된 예약입니다."),
    PAYMENT_ORDER_ID_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT_500_1", "주문번호 생성에 실패했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 내부 오류가 발생했습니다.");

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
