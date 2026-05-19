package kyung.kung_backend.domain.category.repository;

import kyung.kung_backend.domain.category.entity.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, Long> {

    Optional<ServiceCategory> findByNameAndDepthAndParentIsNull(
            String name,
            Long depth
    );

    Optional<ServiceCategory> findByNameAndParentAndDepth(
            String name,
            ServiceCategory parent,
            Long depth
    );
}