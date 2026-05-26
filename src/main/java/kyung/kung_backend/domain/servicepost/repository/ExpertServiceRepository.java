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

    // 중복 서비스 등록 체크
    boolean existsByExpertProfileAndCategoryAndStatus(
            ExpertProfile expertProfile,
            ServiceCategory category,
            String status
    );

    // 고수의 활성 서비스 목록 조회
    List<ExpertService> findAllByExpertProfileAndStatus(
            ExpertProfile expertProfile,
            String status
    );

    // categoryId 기반 중복 체크
    boolean existsByExpertProfileAndCategory_CategoryIdAndStatus(
            ExpertProfile expertProfile,
            Long categoryId,
            String status
    );

    // categoryId 기반 서비스 조회
    Optional<ExpertService> findByExpertProfileAndCategory_CategoryId(
            ExpertProfile expertProfile,
            Long categoryId
    );

    // 상태 기반 전체 조회
    List<ExpertService> findByStatus(String status);

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
}