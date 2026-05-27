package kyung.kung_backend.domain.booking.entity;

import jakarta.persistence.*;
import kyung.kung_backend.domain.location.entity.Location;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LOCATION_ID")
    private Location location;

    @Column(name = "LOCATION_TEXT", length = 255)
    private String locationText;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;

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
            LocalDateTime startAt,
            LocalDateTime endAt,
            Location location,
            String locationText,
            LocalDateTime paymentExpiresAt
    ) {
        Booking booking = new Booking();

        booking.user = user;
        booking.storeProduct = storeProduct;
        booking.startAt = startAt;
        booking.endAt = endAt;
        booking.location = location;
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
