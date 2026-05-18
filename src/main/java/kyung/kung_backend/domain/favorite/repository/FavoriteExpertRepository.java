package kyung.kung_backend.domain.favorite.repository;

import kyung.kung_backend.domain.favorite.entity.FavoriteExpert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteExpertRepository extends JpaRepository<FavoriteExpert, Long> {
}