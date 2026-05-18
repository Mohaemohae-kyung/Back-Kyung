package kyung.kung_backend.domain.servicepay.repository;

import kyung.kung_backend.domain.servicepay.entity.ServicePayHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicePayHistoryRepository extends JpaRepository<ServicePayHistory, Long> {
}