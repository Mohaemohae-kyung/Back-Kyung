package kyung.kung_backend.domain.transaction.repository;

import kyung.kung_backend.domain.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Transaction 엔티티에 대한 DB 접근 Repository입니다.
 *
 * 기본 역할:
 * - 결제, 마켓 구매 등 실제 돈이 오가는 단위를 저장하고 조회합니다.
 *
 * prepare API에서 사용하는 곳:
 * - PaymentService.prepare()가 예약 결제를 위한 거래를 READY 상태로 생성할 때 사용합니다.
 * - 같은 예약에 대해 이미 거래가 있는지 확인해 중복 결제 준비를 막습니다.
 */
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * 특정 예약에 이미 연결된 거래가 있는지 확인합니다.
     *
     * 호출 위치:
     * - PaymentService.validatePaymentNotPrepared()
     *
     * 사용 이유:
     * - TRANSACTIONS.BOOKING_ID에는 unique 제약이 있습니다.
     * - 이 메서드로 먼저 확인하면 DB 제약 오류 대신 PAYMENT_409_1 응답을 명확히 줄 수 있습니다.
     */
    boolean existsByBooking_BookingId(Long bookingId);
}
