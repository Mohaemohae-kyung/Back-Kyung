package kyung.kung_backend.domain.payment.repository;

import kyung.kung_backend.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}