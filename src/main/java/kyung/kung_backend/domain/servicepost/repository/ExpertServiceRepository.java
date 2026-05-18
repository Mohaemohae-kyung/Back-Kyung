package kyung.kung_backend.domain.servicepost.repository;

import kyung.kung_backend.domain.servicepost.entity.ExpertService;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpertServiceRepository extends JpaRepository<ExpertService, Long> {
}