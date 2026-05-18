package kyung.kung_backend.domain.servicepost.entity;

import jakarta.persistence.*;
import kyung.kung_backend.domain.category.entity.ServiceCategory;
import kyung.kung_backend.domain.expert.entity.ExpertProfile;
import kyung.kung_backend.domain.location.entity.Location;
import kyung.kung_backend.global.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "EXPERT_SERVICES")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LOCATION_ID")
    private Location location;

    @Column(name = "TITLE", nullable = false, length = 200)
    private String title;

    @Lob
    @Column(name = "CONTENT", nullable = false)
    private String content;

    @Column(name = "BASE_PRICE", precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;

    @Column(name = "VIEW_COUNT", nullable = false)
    private Long viewCount;

    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;
}