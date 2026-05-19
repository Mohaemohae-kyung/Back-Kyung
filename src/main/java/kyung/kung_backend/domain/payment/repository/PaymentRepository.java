package kyung.kung_backend.domain.payment.repository;

import kyung.kung_backend.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentIdAndUserUserId(
            Long paymentId,
            Long userId
    );

    Optional<Payment> findByTransactionOrderId(String orderId);

    Optional<Payment> findByTransactionTransactionId(Long transactionId);

    Optional<Payment> findByPgPaymentKey(String pgPaymentKey);

    List<Payment> findAllByUserUserIdOrderByCreatedAtDesc(Long userId);
}
