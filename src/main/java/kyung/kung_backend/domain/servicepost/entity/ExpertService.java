package kyung.kung_backend.domain.servicepost.entity;

import jakarta.persistence.*;

import kyung.kung_backend.domain.category.entity.ServiceCategory;
import kyung.kung_backend.domain.expert.entity.ExpertProfile;
import kyung.kung_backend.domain.location.entity.Location;

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

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "EXPERT_PROFILE_ID", nullable = false)
    private ExpertProfile expertProfile;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "CATEGORY_ID", nullable = false)
    private ServiceCategory category;

    // =========================
    // 서비스별 활동 지역 추가
    // =========================

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "LOCATION_ID")
    private Location location;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;

    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    @Column(name = "SERVICE_TITLE", nullable = false, length = 200)
    private String serviceTitle;

    @Column(name = "SERVICE_DESCRIPTION", nullable = false, columnDefinition = "CLOB")
    private String serviceDescription;

    @Column(name = "PRICE")
    private Integer price;

    public static ExpertService create(
            ExpertProfile expertProfile,
            ServiceCategory category,
            Location location,
            String serviceTitle,
            String serviceDescription,
            Integer price
    ) {

        ExpertService expertService = new ExpertService();

        expertService.expertProfile = expertProfile;

        expertService.category = category;

        // =========================
        // 서비스별 지역 저장
        // =========================

        expertService.location = location;

        expertService.serviceTitle = serviceTitle;
        expertService.serviceDescription = serviceDescription;
        expertService.price = price;

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