package kyung.kung_backend.global.exception;

import kyung.kung_backend.domain.payment.exception.PaymentErrorCode;
import kyung.kung_backend.domain.payment.exception.PaymentException;
import kyung.kung_backend.global.response.ApiResponse;
import kyung.kung_backend.global.response.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 애플리케이션 전역 예외 처리 클래스입니다.
 *
 * 왜 필요한지:
 * - Controller 또는 Service에서 예외가 발생했을 때 아무 처리도 하지 않으면 Spring Boot의 기본 /error 경로로 넘어갑니다.
 * - Security가 켜져 있는 프로젝트에서는 이 /error 흐름이 다시 보안 필터를 지나며 Swagger에서 403처럼 보일 수 있습니다.
 * - API 프로젝트에서는 예외도 ApiResponse 형식으로 내려주는 편이 Swagger/Postman 확인에 명확합니다.
 *
 * 호출 흐름:
 * - PaymentService.prepare() 같은 Service에서 IllegalArgumentException 발생
 * - Spring MVC가 이 클래스를 찾아 handleIllegalArgumentException() 호출
 * - 클라이언트/Swagger에는 HTTP 400과 ApiResponse JSON 반환
 *
 * 이후 확장 방향:
 * - 도메인별 커스텀 예외를 만들면 여기에서 ErrorCode와 HTTP 상태를 더 세분화합니다.
 * - 예: PaymentNotFoundException -> 404, DuplicatedPaymentException -> 409
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * @Valid 검증 실패를 처리합니다.
     *
     * 예:
     * - PaymentPrepareRequest.paymentMethod가 비어 있는 경우
     *
     * Swagger에서는 400 응답과 함께 어떤 필드가 잘못됐는지 message로 확인할 수 있습니다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception
    ) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse(ErrorCode.BAD_REQUEST.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.onFailure(ErrorCode.BAD_REQUEST.getCode(), message));
    }

    /**
     * 결제 도메인 전용 예외를 처리합니다.
     *
     * 호출되는 곳:
     * - PaymentService.prepare()에서 PaymentException을 던지는 경우
     *
     * 응답 예:
     * - bookingId가 없으면 HTTP 400 / PAYMENT_400_001
     * - 예약이 없으면 HTTP 404 / PAYMENT_404_001
     * - 이미 결제 준비된 예약이면 HTTP 409 / PAYMENT_409_001
     *
     * 이렇게 분리해두면 Swagger에서 prepare 실패 원인을 공통 BAD_REQUEST가 아니라
     * 결제 도메인 코드로 바로 구분할 수 있습니다.
     */
    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ApiResponse<Void>> handlePaymentException(PaymentException exception) {
        PaymentErrorCode errorCode = exception.getErrorCode();

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.onFailure(errorCode.getCode(), errorCode.getMessage()));
    }

    /**
     * 잘못된 요청 값으로 인해 발생한 예외를 처리합니다.
     *
     * 현재 prepare 스켈레톤에서 사용하는 곳:
     * - bookingId에 해당하는 예약이 없는 경우
     * - matchId에 해당하는 매칭이 없는 경우
     * - bookingId와 matchId가 서로 다른 대상을 가리키는 경우
     * - 결제 가능한 proposedPrice가 없는 경우
     *
     * 지금은 스켈레톤 단계라 IllegalArgumentException을 BAD_REQUEST로 내려줍니다.
     * 이후 커스텀 예외를 만들면 "존재하지 않는 예약" 같은 케이스는 NOT_FOUND로 분리할 수 있습니다.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.onFailure(ErrorCode.BAD_REQUEST.getCode(), exception.getMessage()));
    }

    /**
     * 서버 내부 상태 문제를 처리합니다.
     *
     * 현재 prepare 스켈레톤에서는 주문번호 생성이 여러 번 실패하는 경우 IllegalStateException이 발생할 수 있습니다.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(IllegalStateException exception) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.onFailure(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), exception.getMessage()));
    }
}
