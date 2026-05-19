package kyung.kung_backend.domain.request.repository;

import kyung.kung_backend.domain.request.entity.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {

    List<ServiceRequest> findAllByUserUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long userId);

    Optional<ServiceRequest> findByRequestIdAndDeletedAtIsNull(Long requestId);
}