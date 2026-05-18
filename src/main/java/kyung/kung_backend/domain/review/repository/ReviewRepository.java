package kyung.kung_backend.domain.review.repository;

import kyung.kung_backend.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}