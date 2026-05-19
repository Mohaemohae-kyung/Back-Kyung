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

    // USER
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "USER_409_1", "이미 사용 중인 이메일입니다."),
    DUPLICATE_PHONE(HttpStatus.CONFLICT, "USER_409_2", "이미 사용 중인 전화번호입니다."),
    INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "AUTH_401_1", "아이디 또는 비밀번호가 올바르지 않습니다."),
    DELETED_USER(HttpStatus.UNAUTHORIZED, "AUTH_401_2", "탈퇴한 회원입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 내부 오류가 발생했습니다."),

    // STORE PRODUCT
    STORE_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE_PRODUCT_404_1", "마켓 상품을 찾을 수 없습니다."),
    STORE_PRODUCT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "STORE_PRODUCT_403_1", "해당 상품에 접근할 권한이 없습니다."),
    EXPERT_PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "EXPERT_PROFILE_404_1", "고수 프로필을 찾을 수 없습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CATEGORY_404_1", "카테고리를 찾을 수 없습니다.");

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