package kyung.kung_backend.domain.location.repository;

import kyung.kung_backend.domain.location.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {
}