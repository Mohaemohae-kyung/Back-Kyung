package kyung.kung_backend.domain.servicepost.repository;

import kyung.kung_backend.domain.category.entity.ServiceCategory;
import kyung.kung_backend.domain.expert.entity.ExpertProfile;
import kyung.kung_backend.domain.servicepost.entity.ExpertService;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpertServiceRepository extends JpaRepository<ExpertService, Long> {

    boolean existsByExpertProfileAndCategoryAndStatus(
            ExpertProfile expertProfile,
            ServiceCategory category,
            String status
    );
}