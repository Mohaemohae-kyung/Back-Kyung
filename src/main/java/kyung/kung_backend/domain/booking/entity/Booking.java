package kyung.kung_backend.domain.booking.entity;

import jakarta.persistence.*;
import kyung.kung_backend.domain.expert.entity.ExpertProfile;
import kyung.kung_backend.domain.servicepost.entity.ExpertService;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "BOOKINGS")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "BOOKINGS_SEQ_GENERATOR",
        sequenceName = "BOOKINGS_SEQ",
        allocationSize = 1
)
public class Booking extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BOOKINGS_SEQ_GENERATOR")
    @Column(name = "BOOKING_ID", nullable = false)
    private Long bookingId;

    /*
     * 매칭 기반 예약에서 사용하는 연결값입니다.
     *
     * 결제 API에서는 "서비스 상세 -> 날짜/시간 선택 -> 바로 결제" 흐름도 지원해야 하므로
     * MATCH_ID는 nullable로 둡니다. 기존 견적/매칭 플로우에서 생성된 예약은 이 값을 채우고,
     * 마켓형 즉시 예약은 아래 expertService 값을 기준으로 생성됩니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MATCH_ID", unique = true)
    private Match match;

    /*
     * 마켓형 예약에서 사용자가 선택한 실제 서비스입니다.
     * 결제 화면에서 상품명/가격을 다시 계산할 때 이 값을 사용합니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EXPERT_SERVICE_ID")
    private ExpertService expertService;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "EXPERT_PROFILE_ID", nullable = false)
    private ExpertProfile expertProfile;

    @Column(name = "START_AT", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "END_AT")
    private LocalDateTime endAt;

    @Column(name = "LOCATION_TEXT", length = 255)
    private String locationText;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;

    /*
     * 결제 전 임시 예약이 슬롯을 너무 오래 점유하지 않도록 만료 시각을 저장합니다.
     * 결제 준비/승인 API는 이 값을 확인해서 만료된 예약을 결제하지 못하게 막습니다.
     */
    @Column(name = "PAYMENT_EXPIRES_AT")
    private LocalDateTime paymentExpiresAt;

    @Column(name = "CANCELLED_AT")
    private LocalDateTime cancelledAt;

    public static final String STATUS_PENDING_PAYMENT = "PENDING_PAYMENT";
    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_EXPIRED = "EXPIRED";

    public static Booking createPendingPayment(
            User user,
            ExpertService expertService,
            LocalDateTime startAt,
            LocalDateTime endAt,
            String locationText,
            LocalDateTime paymentExpiresAt
    ) {
        Booking booking = new Booking();

        booking.match = null;
        booking.expertService = expertService;
        booking.user = user;
        booking.expertProfile = expertService.getExpertProfile();
        booking.startAt = startAt;
        booking.endAt = endAt;
        booking.locationText = locationText;
        booking.status = STATUS_PENDING_PAYMENT;
        booking.paymentExpiresAt = paymentExpiresAt;
        booking.cancelledAt = null;

        return booking;
    }

    public boolean isOwnedBy(User user) {
        return user != null && this.user.getUserId().equals(user.getUserId());
    }

    public boolean isPendingPayment() {
        return STATUS_PENDING_PAYMENT.equals(this.status);
    }

    public boolean isConfirmed() {
        return STATUS_CONFIRMED.equals(this.status);
    }

    public boolean isPaymentExpired(LocalDateTime now) {
        return paymentExpiresAt != null && paymentExpiresAt.isBefore(now);
    }

    public void confirmPayment() {
        this.status = STATUS_CONFIRMED;
        this.paymentExpiresAt = null;
    }

    public void expirePayment() {
        this.status = STATUS_EXPIRED;
    }

    public void cancel() {
        this.status = STATUS_CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.paymentExpiresAt = null;
    }
}

    /*
     * 매칭 기반 예약에서 사용하는 연결값입니다.
     *
     * 결제 API에서는 "서비스 상세 -> 날짜/시간 선택 -> 바로 결제" 흐름도 지원해야 하므로
     * MATCH_ID는 nullable로 둡니다. 기존 견적/매칭 플로우에서 생성된 예약은 이 값을 채우고,
     * 마켓형 즉시 예약은 아래 expertService 값을 기준으로 생성됩니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MATCH_ID", unique = true)
    private Match match;

    /*
     * 마켓형 예약에서 사용자가 선택한 실제 서비스입니다.
     * 결제 화면에서 상품명/가격을 다시 계산할 때 이 값을 사용합니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EXPERT_SERVICE_ID")
    private ExpertService expertService;
