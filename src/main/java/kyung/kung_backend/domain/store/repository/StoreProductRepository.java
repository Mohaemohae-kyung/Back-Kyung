package kyung.kung_backend.domain.store.repository;

import kyung.kung_backend.domain.store.entity.StoreProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreProductRepository extends JpaRepository<StoreProduct, Long> {
}