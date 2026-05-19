package kyung.kung_backend.domain.booking.entity;

import jakarta.persistence.*;
import kyung.kung_backend.domain.store.entity.StoreProduct;
import kyung.kung_backend.domain.expert.entity.ExpertProfile;
import kyung.kung_backend.domain.servicepost.entity.ExpertService;
import kyung.kung_backend.domain.store.entity.StoreProduct;
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
     * 마켓 상품 예약 연결값입니다.
     * 숨고 마켓처럼 고수가 올린 상품(StoreProduct)을 사용자가 날짜/시간 선택 후 예약할 때 사용합니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STORE_PRODUCT_ID")
    private StoreProduct storeProduct;

    /*
     * 이전 서비스 게시글 기반 예약을 위한 연결값입니다.
     * main 병합 후 마켓 결제는 StoreProduct를 우선 사용하지만, 기존 Swagger/개발 테스트 호환을 위해 남겨둡니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EXPERT_SERVICE_ID")
    private ExpertService expertService;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "STORE_PRODUCT_ID", nullable = false)
    private StoreProduct storeProduct;

    @Column(name = "START_AT", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "END_AT")
    private LocalDateTime endAt;

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

    public static Booking createPendingPaymentForStoreProduct(
            User user,
            StoreProduct storeProduct,
            ExpertProfile expertProfile,
            LocalDateTime startAt,
            LocalDateTime endAt,
            String locationText,
            LocalDateTime paymentExpiresAt
    ) {
        Booking booking = new Booking();

        booking.storeProduct = storeProduct;
        booking.expertService = null;
        booking.user = user;
        booking.expertProfile = expertProfile;
        booking.startAt = startAt;
        booking.endAt = endAt;
        booking.locationText = locationText;
        booking.status = STATUS_PENDING_PAYMENT;
        booking.paymentExpiresAt = paymentExpiresAt;
        booking.cancelledAt = null;

        return booking;
    }

    public static Booking createPendingPayment(
            User user,
            ExpertService expertService,
            LocalDateTime startAt,
            LocalDateTime endAt,
            String locationText,
            LocalDateTime paymentExpiresAt
    ) {
        Booking booking = new Booking();

        booking.storeProduct = null;
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
