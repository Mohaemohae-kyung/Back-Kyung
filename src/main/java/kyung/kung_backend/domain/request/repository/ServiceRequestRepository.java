package kyung.kung_backend.domain.request.repository;

import kyung.kung_backend.domain.request.entity.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
}