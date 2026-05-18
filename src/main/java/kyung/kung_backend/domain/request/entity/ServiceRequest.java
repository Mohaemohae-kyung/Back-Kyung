package kyung.kung_backend.domain.request.entity;

import jakarta.persistence.*;
import kyung.kung_backend.domain.category.entity.ServiceCategory;
import kyung.kung_backend.domain.location.entity.Location;
import kyung.kung_backend.domain.servicepost.entity.ExpertService;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "SERVICE_REQUESTS")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "SERVICE_REQUESTS_SEQ_GENERATOR",
        sequenceName = "SERVICE_REQUESTS_SEQ",
        allocationSize = 1
)
public class ServiceRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SERVICE_REQUESTS_SEQ_GENERATOR")
    @Column(name = "REQUEST_ID", nullable = false)
    private Long requestId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CATEGORY_ID", nullable = false)
    private ServiceCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LOCATION_ID")
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EXPERT_SERVICE_ID")
    private ExpertService expertService;

    @Column(name = "TITLE", nullable = false, length = 200)
    private String title;

    @Lob
    @Column(name = "CONTENT", nullable = false)
    private String content;

    @Column(name = "BUDGET", precision = 12, scale = 2)
    private BigDecimal budget;

    @Column(name = "PREFERRED_DATE")
    private LocalDateTime preferredDate;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;

    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    private static final String STATUS_OPEN = "OPEN";
    private static final String STATUS_CANCELLED = "CANCELLED";

    public static ServiceRequest create(
            User user,
            ServiceCategory category,
            Location location,
            ExpertService expertService,
            String title,
            String content,
            BigDecimal budget,
            LocalDateTime preferredDate
    ) {
        ServiceRequest serviceRequest = new ServiceRequest();

        serviceRequest.user = user;
        serviceRequest.category = category;
        serviceRequest.location = location;
        serviceRequest.expertService = expertService;
        serviceRequest.title = title;
        serviceRequest.content = content;
        serviceRequest.budget = budget;
        serviceRequest.preferredDate = preferredDate;
        serviceRequest.status = STATUS_OPEN;
        serviceRequest.deletedAt = null;

        return serviceRequest;
    }

    public void update(
            String title,
            String content,
            BigDecimal budget,
            LocalDateTime preferredDate
    ) {
        if (title != null && !title.isBlank()) {
            this.title = title;
        }

        if (content != null && !content.isBlank()) {
            this.content = content;
        }

        if (budget != null) {
            this.budget = budget;
        }

        if (preferredDate != null) {
            this.preferredDate = preferredDate;
        }
    }

    public void cancel() {
        this.status = STATUS_CANCELLED;
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isCancelled() {
        return STATUS_CANCELLED.equals(this.status);
    }
}