package kyung.kung_backend.domain.booking.repository;

import kyung.kung_backend.domain.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByUserUserIdOrderByCreatedAtDesc(Long userId);

    List<Booking> findAllByStatusAndPaymentExpiresAtBefore(
            String status,
            LocalDateTime now
    );

    boolean existsByExpertServiceExpertServiceIdAndStartAtLessThanAndEndAtGreaterThanAndStatusIn(
            Long expertServiceId,
            LocalDateTime endAt,
            LocalDateTime startAt,
            Collection<String> statuses
    );

    boolean existsByStoreProductStoreProductIdAndStartAtLessThanAndEndAtGreaterThanAndStatusIn(
            Long storeProductId,
            LocalDateTime endAt,
            LocalDateTime startAt,
            Collection<String> statuses
    );
}
