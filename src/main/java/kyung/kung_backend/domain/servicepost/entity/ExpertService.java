package kyung.kung_backend.domain.servicepost.entity;

import jakarta.persistence.*;
import kyung.kung_backend.domain.category.entity.ServiceCategory;
import kyung.kung_backend.domain.expert.entity.ExpertProfile;
import kyung.kung_backend.global.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "EXPERT_PROFILE_ID", nullable = false)
    private ExpertProfile expertProfile;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "CATEGORY_ID", nullable = false)
    private ServiceCategory category;

    @Column(name = "SERVICE_TITLE", nullable = false)
    private String serviceTitle;

    @Column(name = "SERVICE_DESCRIPTION")
    private String serviceDescription;

    @Column(name = "STATUS", nullable = false)
    private String status;

    public static ExpertService create(
            ExpertProfile expertProfile,
            ServiceCategory category
    ) {

        ExpertService expertService = new ExpertService();

        expertService.expertProfile = expertProfile;
        expertService.category = category;

        expertService.serviceTitle = "기본 서비스";
        expertService.serviceDescription = "";
        expertService.status = "ACTIVE";

        return expertService;
    }
}