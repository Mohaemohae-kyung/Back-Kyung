package kyung.kung_backend.domain.request.repository;

import kyung.kung_backend.domain.request.entity.ServiceRequest;
import kyung.kung_backend.domain.request.enums.RequestStatus;
import kyung.kung_backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {

    List<ServiceRequest> findAllByUserUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long userId);

    Optional<ServiceRequest> findByRequestIdAndDeletedAtIsNull(Long requestId);

    long countByUserAndStatusInAndDeletedAtIsNull(User user, List<RequestStatus> statuses);

    @Query("""
            select sr
            from ServiceRequest sr
            join fetch sr.user u
            join fetch sr.expertService es
            join fetch es.expertProfile ep
            left join fetch es.category c
            left join fetch ep.mainLocation ml
            where ep.user.userId = :expertUserId
            and sr.deletedAt is null
            order by sr.createdAt desc
            """)
    List<ServiceRequest> findAllReceivedByExpertUserId(
            @Param("expertUserId") Long expertUserId
    );
}