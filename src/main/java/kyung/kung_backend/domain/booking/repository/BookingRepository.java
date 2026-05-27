package kyung.kung_backend.domain.booking.repository;

import kyung.kung_backend.domain.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByUserUserIdOrderByCreatedAtDesc(Long userId);

    List<Booking> findAllByStatusAndPaymentExpiresAtBefore(
            String status,
            LocalDateTime now
    );

    boolean existsByStoreProductStoreProductIdAndStartAtLessThanAndEndAtGreaterThanAndStatusIn(
            Long storeProductId,
            LocalDateTime endAt,
            LocalDateTime startAt,
            Collection<String> statuses
    );

    @Query("""
            select count(b) > 0
            from Booking b
            where b.storeProduct.storeProductId = :storeProductId
              and b.startAt < :endAt
              and b.endAt > :startAt
              and (
                    b.status = 'CONFIRMED'
                    or (b.status = 'PENDING_PAYMENT' and b.paymentExpiresAt > :now)
                  )
            """)
    boolean existsActiveBlockingReservation(
            @Param("storeProductId") Long storeProductId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("now") LocalDateTime now
    );
}
