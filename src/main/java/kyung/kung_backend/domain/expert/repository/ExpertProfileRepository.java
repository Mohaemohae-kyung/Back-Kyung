package kyung.kung_backend.domain.expert.repository;

import kyung.kung_backend.domain.expert.entity.ExpertProfile;
import kyung.kung_backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExpertProfileRepository
        extends JpaRepository<ExpertProfile, Long> {

    boolean existsByUser(User user);

    Optional<ExpertProfile> findByUser(User user);

    // 추가
    Optional<ExpertProfile> findByUser_UserId(Long userId);

    Optional<ExpertProfile> findById(Long expertProfileId);
}