package kyung.kung_backend.domain.payment.pg.repository;

import kyung.kung_backend.domain.payment.pg.entity.MockPgPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MockPgPaymentRepository extends JpaRepository<MockPgPayment, Long> {

    Optional<MockPgPayment> findByOrderId(String orderId);

    Optional<MockPgPayment> findByPaymentKey(String paymentKey);
}
