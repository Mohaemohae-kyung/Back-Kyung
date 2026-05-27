package kyung.kung_backend.domain.favorite.repository;

import kyung.kung_backend.domain.expert.entity.ExpertProfile;
import kyung.kung_backend.domain.favorite.entity.FavoriteExpert;
import kyung.kung_backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FavoriteExpertRepository extends JpaRepository<FavoriteExpert, Long> {

    boolean existsByUserAndExpertProfile(User user, ExpertProfile expertProfile);

    void deleteByUserAndExpertProfile(User user, ExpertProfile expertProfile);

    long countByUser(User user);

    @Query("""
            select fe
            from FavoriteExpert fe
            join fetch fe.expertProfile ep
            left join fetch ep.mainCategory
            where fe.user = :user
            order by fe.createdAt desc
            """)
    List<FavoriteExpert> findAllByUserWithExpertProfile(User user);
}