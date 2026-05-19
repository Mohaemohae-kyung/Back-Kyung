package kyung.kung_backend.domain.store.repository;

import kyung.kung_backend.domain.store.entity.StoreProduct;
import kyung.kung_backend.domain.store.entity.enums.StoreProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoreProductRepository extends JpaRepository<StoreProduct, Long> {

    List<StoreProduct> findAllByStatus(StoreProductStatus status);

    List<StoreProduct> findAllByCategory_CategoryIdAndStatus(
            Long categoryId,
            StoreProductStatus status
    );

    Optional<StoreProduct> findByStoreProductIdAndStatus(
            Long storeProductId,
            StoreProductStatus status
    );

    List<StoreProduct> findAllByExpertProfile_ExpertProfileIdAndStatus(
            Long expertProfileId,
            StoreProductStatus status
    );
}