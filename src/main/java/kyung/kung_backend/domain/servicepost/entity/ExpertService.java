package kyung.kung_backend.domain.servicepost.entity;

import jakarta.persistence.*;
import kyung.kung_backend.domain.category.entity.ServiceCategory;
import kyung.kung_backend.domain.expert.entity.ExpertProfile;
import kyung.kung_backend.global.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "EXPERT_SERVICES",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_EXPERT_SERVICES_PROFILE_CATEGORY",
                        columnNames = {"EXPERT_PROFILE_ID", "CATEGORY_ID"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "EXPERT_SERVICES_SEQ_GENERATOR",
        sequenceName = "EXPERT_SERVICES_SEQ",
        allocationSize = 1
)
public class ExpertService extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "EXPERT_SERVICES_SEQ_GENERATOR")
    @Column(name = "EXPERT_SERVICE_ID", nullable = false)
    private Long expertServiceId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "EXPERT_PROFILE_ID", nullable = false)
    private ExpertProfile expertProfile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CATEGORY_ID", nullable = false)
    private ServiceCategory category;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;

    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    public static ExpertService create(
            ExpertProfile expertProfile,
            ServiceCategory category
    ) {
        ExpertService expertService = new ExpertService();

        expertService.expertProfile = expertProfile;
        expertService.category = category;
        expertService.status = "ACTIVE";
        expertService.deletedAt = null;

        return expertService;
    }

    public void delete() {
        this.status = "DELETED";
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return "ACTIVE".equals(this.status);
    }

    public boolean isDeleted() {
        return "DELETED".equals(this.status);
    }
}