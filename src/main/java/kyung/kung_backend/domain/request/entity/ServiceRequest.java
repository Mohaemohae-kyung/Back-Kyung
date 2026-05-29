package kyung.kung_backend.domain.request.entity;

import jakarta.persistence.*;
import kyung.kung_backend.domain.category.entity.ServiceCategory;
import kyung.kung_backend.domain.expert.entity.ExpertProfile;
import kyung.kung_backend.domain.request.enums.RequestStatus;
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
    @JoinColumn(name = "EXPERT_PROFILE_ID", nullable = false)
    private ExpertProfile expertProfile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CATEGORY_ID", nullable = false)
    private ServiceCategory category;

    @Column(name = "TITLE", nullable = false, length = 200)
    private String title;

    @Lob
    @Column(name = "CONTENT", nullable = false)
    private String content;

    @Column(name = "BUDGET", precision = 12, scale = 2)
    private BigDecimal budget;

    @Column(name = "PREFERRED_DATE")
    private LocalDateTime preferredDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private RequestStatus status;

    @Column(name = "RESPONDED_AT")
    private LocalDateTime respondedAt;

    @Column(name = "REJECT_REASON", length = 500)
    private String rejectReason;

    @Column(name = "COMPLETED_AT")
    private LocalDateTime completedAt;

    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    @Column(name = "PAYMENT_MODE", length = 20)
    private String paymentMode;

    public static ServiceRequest create(
            User user,
            ExpertProfile expertProfile,
            ServiceCategory category,
            String title,
            String content,
            BigDecimal budget,
            LocalDateTime preferredDate
    ) {
        ServiceRequest serviceRequest = new ServiceRequest();

        serviceRequest.user = user;
        serviceRequest.expertProfile = expertProfile;
        serviceRequest.category = category;
        serviceRequest.title = title;
        serviceRequest.content = content;
        serviceRequest.budget = budget;
        serviceRequest.preferredDate = preferredDate;
        serviceRequest.status = RequestStatus.PENDING;
        serviceRequest.deletedAt = null;

        return serviceRequest;
    }

    public void update(
            String title,
            String content,
            BigDecimal budget,
            LocalDateTime preferredDate,
            String paymentMode
    ) {
        if (!isPending() && !isChatting()) {

            throw new IllegalStateException(
                    "진행 중이거나 대기 중인 견적 요청만 수정할 수 있습니다."
            );
        }

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

        if (paymentMode != null && !paymentMode.isBlank()) {
            this.paymentMode = paymentMode;
        }
    }

    public void startChatting() {
        if (!isPending() && !isChatting()) {

            throw new IllegalStateException(
                    "진행 중이거나 대기 중인 견적 요청만 수정할 수 있습니다."
            );
        }

        this.status = RequestStatus.CHATTING;
        this.respondedAt = LocalDateTime.now();
    }

    public void reject(String rejectReason) {
        if (!isPending()) {
            throw new IllegalStateException("대기 중인 견적 요청만 거절할 수 있습니다.");
        }

        this.status = RequestStatus.REJECTED;
        this.rejectReason = rejectReason;
        this.respondedAt = LocalDateTime.now();
    }

    public void complete() {
        if (!isChatting()) {
            throw new IllegalStateException("채팅 중인 견적 요청만 완료 처리할 수 있습니다.");
        }

        this.status = RequestStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (!isPending()) {
            throw new IllegalStateException("대기 중인 견적 요청만 취소할 수 있습니다.");
        }

        this.status = RequestStatus.CANCELLED;
        this.deletedAt = LocalDateTime.now();
    }

    public void delete() {
        this.status = RequestStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isPending() {
        return this.status == RequestStatus.PENDING;
    }

    public boolean isChatting() {
        return this.status == RequestStatus.CHATTING;
    }

    public boolean isCompleted() {
        return this.status == RequestStatus.COMPLETED;
    }

    public boolean isRejected() {
        return this.status == RequestStatus.REJECTED;
    }

    public boolean isCancelled() {
        return this.status == RequestStatus.CANCELLED;
    }

    public boolean isDeleted() {
        return this.status == RequestStatus.DELETED;
    }
}