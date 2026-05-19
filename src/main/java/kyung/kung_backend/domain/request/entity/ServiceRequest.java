package kyung.kung_backend.domain.request.entity;

import jakarta.persistence.*;
import kyung.kung_backend.domain.category.entity.ServiceCategory;
import kyung.kung_backend.domain.location.entity.Location;
import kyung.kung_backend.domain.request.enums.RequestStatus;
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
        serviceRequest.status = RequestStatus.PENDING;
        serviceRequest.deletedAt = null;

        return serviceRequest;
    }

    public void update(
            String title,
            String content,
            BigDecimal budget,
            LocalDateTime preferredDate
    ) {
        if (!isPending()) {
            throw new IllegalStateException("대기 중인 견적 요청만 수정할 수 있습니다.");
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
    }

    // 고수가 견적 요청을 승인하면 곧바로 채팅방 생성 흐름으로 진입한다.
    public void startChatting() {
        if (!isPending()) {
            throw new IllegalStateException("대기 중인 견적 요청만 승인할 수 있습니다.");
        }

        this.status = RequestStatus.CHATTING;
        this.respondedAt = LocalDateTime.now();
    }

    // 고수가 견적 요청을 거절한다.
    public void reject(String rejectReason) {
        if (!isPending()) {
            throw new IllegalStateException("대기 중인 견적 요청만 거절할 수 있습니다.");
        }

        this.status = RequestStatus.REJECTED;
        this.rejectReason = rejectReason;
        this.respondedAt = LocalDateTime.now();
    }

    // 채팅/결제/거래가 최종 완료된 상태로 변경한다.
    public void complete() {
        if (!isChatting()) {
            throw new IllegalStateException("채팅 중인 견적 요청만 완료 처리할 수 있습니다.");
        }

        this.status = RequestStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    // 사용자가 견적 요청을 취소한다.
    public void cancel() {
        if (!isPending()) {
            throw new IllegalStateException("대기 중인 견적 요청만 취소할 수 있습니다.");
        }

        this.status = RequestStatus.CANCELLED;
        this.deletedAt = LocalDateTime.now();
    }

    // 관리자 또는 작성자가 소프트 삭제 처리한다.
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