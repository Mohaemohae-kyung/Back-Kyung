package kyung.kung_backend.domain.location.repository;

import kyung.kung_backend.domain.location.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {

    Optional<Location> findByNameAndDepthAndParentIsNull(
            String name,
            Long depth
    );

    Optional<Location> findByNameAndParentAndDepth(
            String name,
            Location parent,
            Long depth
    );
}