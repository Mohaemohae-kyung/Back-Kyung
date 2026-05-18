package kyung.kung_backend.domain.booking.repository;

import kyung.kung_backend.domain.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
}