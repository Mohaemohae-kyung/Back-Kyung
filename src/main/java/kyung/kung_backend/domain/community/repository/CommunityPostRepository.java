package kyung.kung_backend.domain.community.repository;

import kyung.kung_backend.domain.community.entity.CommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {
}