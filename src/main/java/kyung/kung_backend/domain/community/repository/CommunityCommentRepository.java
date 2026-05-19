package kyung.kung_backend.domain.community.repository;

import kyung.kung_backend.domain.community.entity.CommunityComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {
    List<CommunityComment> findByCommunityPostCommunityPostIdAndStatus(Long communityPostId, String status);
}