package kyung.kung_backend.domain.booking.repository;

import kyung.kung_backend.domain.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Booking 엔티티 DB 접근 Repository입니다.
 *
 * 호출 위치:
 * - PaymentService.prepare()에서 bookingId로 결제 대상 예약을 조회할 때 사용합니다.
 * - 조회된 Booking의 Match 정보를 통해 결제 금액(proposedPrice)을 계산합니다.
 */
public interface BookingRepository extends JpaRepository<Booking, Long> {
}
