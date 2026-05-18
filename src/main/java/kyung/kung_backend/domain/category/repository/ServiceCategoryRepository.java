package kyung.kung_backend.domain.category.repository;

import kyung.kung_backend.domain.category.entity.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, Long> {
}