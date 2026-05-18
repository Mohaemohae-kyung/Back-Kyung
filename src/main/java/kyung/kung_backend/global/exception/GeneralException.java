package kyung.kung_backend.global.exception;

import kyung.kung_backend.global.response.ErrorCode;
import kyung.kung_backend.global.response.ReasonDto;
import lombok.Getter;

// 공통 예외 처리
@Getter
public class GeneralException extends RuntimeException {

    private final ErrorCode code;

    private GeneralException(ErrorCode code) {
        super(code.getReason().getMessage());
        this.code = code;
    }

    // 예외 생성
    public static GeneralException of(ErrorCode code) {
        return new GeneralException(code);
    }

    // 예외 상세 정보
    public ReasonDto getReason() {
        return this.code.getReason();
    }

    public String getErrorCode() {
        return this.code.getReason().getCode();
    }
}