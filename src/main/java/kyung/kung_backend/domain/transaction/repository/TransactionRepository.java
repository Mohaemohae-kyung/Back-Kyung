package kyung.kung_backend.domain.transaction.repository;

import kyung.kung_backend.domain.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Transaction 엔티티 DB 접근 Repository입니다.
 *
 * 호출 위치:
 * - PaymentService.prepare()가 결제 준비 시 거래 단위를 먼저 만들고 저장할 때 사용합니다.
 * - Payment는 Transaction을 참조하므로 prepare 흐름에서 Transaction 저장이 Payment 저장보다 먼저 일어납니다.
 */
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * 특정 예약에 대해 이미 생성된 거래가 있는지 확인합니다.
     *
     * 호출 위치:
     * - PaymentService.prepare()에서 같은 bookingId로 결제 준비를 중복 생성하지 않기 위해 사용합니다.
     *
     * 왜 필요한지:
     * - TRANSACTIONS 테이블에는 BOOKING_ID unique 제약이 있습니다.
     * - Service에서 먼저 확인하지 않으면 중복 prepare 요청이 DB 제약 오류로 터집니다.
     * - 이 메서드로 사전에 확인하면 Swagger에는 PAYMENT_409_001 같은 이해 가능한 응답을 줄 수 있습니다.
     */
    boolean existsByBooking_BookingId(Long bookingId);
}
