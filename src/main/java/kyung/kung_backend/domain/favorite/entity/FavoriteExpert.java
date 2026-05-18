package kyung.kung_backend.domain.favorite.entity;

import jakarta.persistence.*;
import kyung.kung_backend.domain.expert.entity.ExpertProfile;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.common.BaseCreatedEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "FAVORITE_EXPERTS",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_FAVORITE_EXPERTS",
                        columnNames = {"USER_ID", "EXPERT_PROFILE_ID"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "FAVORITE_EXPERTS_SEQ_GENERATOR",
        sequenceName = "FAVORITE_EXPERTS_SEQ",
        allocationSize = 1
)
public class FavoriteExpert extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FAVORITE_EXPERTS_SEQ_GENERATOR")
    @Column(name = "FAVORITE_EXPERT_ID", nullable = false)
    private Long favoriteExpertId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "EXPERT_PROFILE_ID", nullable = false)
    private ExpertProfile expertProfile;
}