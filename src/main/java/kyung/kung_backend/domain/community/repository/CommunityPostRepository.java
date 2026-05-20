package kyung.kung_backend.domain.community.repository;

import kyung.kung_backend.domain.community.entity.CommunityPost;
import kyung.kung_backend.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {

    Page<CommunityPost> findByStatus(String status, Pageable pageable);

    long countByUserAndStatus(User user, String status);
}