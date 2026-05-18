package kyung.kung_backend.global.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import kyung.kung_backend.global.response.ApiResponse;
import kyung.kung_backend.global.response.ErrorCode;
import kyung.kung_backend.global.response.ReasonDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // GeneralException 처리
    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResponse<?>> handleGeneralException(GeneralException e) {
        ReasonDto reason = e.getReason();

        return ResponseEntity
                .status(reason.getHttpStatus())
                .body(ApiResponse.onFailure(e.getCode()));
    }

    // @RequestBody 검증 실패 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        e.getBindingResult().getFieldErrors().forEach(fieldError -> {
            String fieldName = fieldError.getField();
            String errorMessage = Optional.ofNullable(fieldError.getDefaultMessage())
                    .orElse("잘못된 요청입니다.");

            errors.put(fieldName, errorMessage);
        });

        ReasonDto reason = ErrorCode.BAD_REQUEST.getReason();

        return ResponseEntity
                .status(reason.getHttpStatus())
                .body(ApiResponse.onFailure(ErrorCode.BAD_REQUEST, errors));
    }

    // @RequestParam, @PathVariable 검증 실패 처리
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleConstraintViolationException(
            ConstraintViolationException e
    ) {
        String errorMessage = e.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElse("잘못된 요청입니다.");

        ReasonDto reason = ErrorCode.BAD_REQUEST.getReason();

        return ResponseEntity
                .status(reason.getHttpStatus())
                .body(ApiResponse.onFailure(ErrorCode.BAD_REQUEST, errorMessage));
    }

    // IllegalArgumentException 처리
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgumentException(
            IllegalArgumentException e
    ) {
        ReasonDto reason = ErrorCode.BAD_REQUEST.getReason();

        return ResponseEntity
                .status(reason.getHttpStatus())
                .body(ApiResponse.onFailure(ErrorCode.BAD_REQUEST, e.getMessage()));
    }

    // ResponseStatusException 처리
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<?>> handleResponseStatusException(
            ResponseStatusException e
    ) {
        return ResponseEntity
                .status(e.getStatusCode())
                .body(ApiResponse.onFailure(
                        ErrorCode.UNAUTHORIZED,
                        e.getReason()
                ));
    }

    // 그 외 예상하지 못한 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleUnhandledException(Exception e) {
        log.error("Unhandled exception occurred", e);

        ReasonDto reason = ErrorCode.INTERNAL_SERVER_ERROR.getReason();

        return ResponseEntity
                .status(reason.getHttpStatus())
                .body(ApiResponse.onFailure(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage()));
    }
}