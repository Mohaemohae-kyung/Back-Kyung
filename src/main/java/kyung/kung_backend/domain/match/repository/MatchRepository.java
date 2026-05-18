package kyung.kung_backend.domain.match.repository;

import kyung.kung_backend.domain.match.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<Match, Long> {
}