package kyung.kung_backend.domain.servicepost.repository;

import kyung.kung_backend.domain.category.entity.ServiceCategory;
import kyung.kung_backend.domain.expert.entity.ExpertProfile;
import kyung.kung_backend.domain.servicepost.entity.ExpertService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExpertServiceRepository extends JpaRepository<ExpertService, Long> {

    List<ExpertService> findAllByExpertProfileAndStatus(
            ExpertProfile expertProfile,
            String status
    );

    boolean existsByExpertProfileAndCategory_CategoryIdAndStatus(
            ExpertProfile expertProfile,
            Long categoryId,
            String status
    );

    Optional<ExpertService> findByExpertProfileAndCategory_CategoryId(
            ExpertProfile expertProfile,
            Long categoryId
    );
}