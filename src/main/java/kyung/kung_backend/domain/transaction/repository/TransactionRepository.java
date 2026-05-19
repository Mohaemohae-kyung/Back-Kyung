package kyung.kung_backend.domain.transaction.repository;

import kyung.kung_backend.domain.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByOrderId(String orderId);

    Optional<Transaction> findByBookingBookingIdAndStatus(
            Long bookingId,
            String status
    );

    Optional<Transaction> findFirstByBookingBookingIdAndStatusOrderByCreatedAtDesc(
            Long bookingId,
            String status
    );
}
