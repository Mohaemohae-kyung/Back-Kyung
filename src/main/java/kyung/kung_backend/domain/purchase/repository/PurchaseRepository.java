package kyung.kung_backend.domain.purchase.repository;

import kyung.kung_backend.domain.purchase.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
}