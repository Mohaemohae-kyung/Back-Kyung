package kyung.kung_backend.domain.transaction.repository;

import kyung.kung_backend.domain.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}