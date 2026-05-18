package kyung.kung_backend.domain.expert.repository;

import kyung.kung_backend.domain.expert.entity.ExpertProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpertProfileRepository extends JpaRepository<ExpertProfile, Long> {
}