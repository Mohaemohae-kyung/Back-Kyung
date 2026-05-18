package kyung.kung_backend.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {

    private final Boolean isSuccess;
    private final String code;
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T result;

    public static <T> ApiResponse<T> onSuccess(T result) {
        ReasonDto reason = SuccessCode.OK.getReason();

        return new ApiResponse<>(
                true,
                reason.getCode(),
                reason.getMessage(),
                result
        );
    }

    public static <T> ApiResponse<T> onSuccess(SuccessCode successCode, T result) {
        ReasonDto reason = successCode.getReason();

        return new ApiResponse<>(
                true,
                reason.getCode(),
                reason.getMessage(),
                result
        );
    }

    public static ApiResponse<Void> onSuccess(SuccessCode successCode) {
        ReasonDto reason = successCode.getReason();

        return new ApiResponse<>(
                true,
                reason.getCode(),
                reason.getMessage(),
                null
        );
    }

    public static ApiResponse<Void> onFailure(ErrorCode errorCode) {
        ReasonDto reason = errorCode.getReason();

        return new ApiResponse<>(
                false,
                reason.getCode(),
                reason.getMessage(),
                null
        );
    }

    public static <T> ApiResponse<T> onFailure(ErrorCode errorCode, T result) {
        ReasonDto reason = errorCode.getReason();

        return new ApiResponse<>(
                false,
                reason.getCode(),
                reason.getMessage(),
                result
        );
    }

    public static ApiResponse<Void> onFailure(String code, String message) {
        return new ApiResponse<>(
                false,
                code,
                message,
                null
        );
    }
}