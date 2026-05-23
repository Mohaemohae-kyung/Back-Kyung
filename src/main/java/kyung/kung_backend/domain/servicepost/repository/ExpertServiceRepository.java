package kyung.kung_backend.domain.servicepost.repository;

import kyung.kung_backend.domain.category.entity.ServiceCategory;
import kyung.kung_backend.domain.expert.entity.ExpertProfile;
import kyung.kung_backend.domain.servicepost.entity.ExpertService;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExpertServiceRepository
        extends JpaRepository<ExpertService, Long> {

    boolean existsByExpertProfileAndCategoryAndStatus(
            ExpertProfile expertProfile,
            ServiceCategory category,
            String status
    );

    List<ExpertService> findByStatus(String status);

    @Query("""
        select es
        from ExpertService es
        join fetch es.expertProfile ep
        left join fetch ep.mainCategory
        left join fetch ep.mainLocation
        where es.expertServiceId = :id
    """)
    Optional<ExpertService> findDetailById(
            @Param("id") Long id
    );
}