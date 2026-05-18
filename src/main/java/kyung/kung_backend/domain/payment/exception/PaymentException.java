package kyung.kung_backend.domain.payment.exception;

import lombok.Getter;

/**
 * 결제 도메인 전용 RuntimeException입니다.
 *
 * 사용 흐름:
 * - PaymentService에서 결제 관련 검증이 실패하면 new PaymentException(PaymentErrorCode.xxx)를 던집니다.
 * - GlobalExceptionHandler가 이 예외를 잡아 HTTP 상태 코드와 ApiResponse JSON으로 변환합니다.
 *
 * 왜 RuntimeException인지:
 * - Service 메서드마다 throws 선언을 반복하지 않기 위해 unchecked exception으로 둡니다.
 * - Spring @Transactional은 RuntimeException이 발생하면 기본적으로 트랜잭션을 rollback합니다.
 * - prepare 중간에 Transaction 또는 Payment 저장이 실패하면 DB 변경이 남지 않도록 하는 데도 맞습니다.
 */
@Getter
public class PaymentException extends RuntimeException {

    private final PaymentErrorCode errorCode;

    public PaymentException(PaymentErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
