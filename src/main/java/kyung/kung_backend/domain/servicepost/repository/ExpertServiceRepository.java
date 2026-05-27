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
    // 상세 조회
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

    // 고수 프로필 기준 첫 번째 서비스 조회
    Optional<ExpertService> findFirstByExpertProfileOrderByExpertServiceIdAsc(
            ExpertProfile expertProfile
    );

    List<ExpertService> findAllByExpertProfile(ExpertProfile expertProfile);

    void deleteByExpertProfile(ExpertProfile expertProfile);

    boolean existsByExpertProfileAndCategory(
            ExpertProfile expertProfile,
            ServiceCategory category
    );
}