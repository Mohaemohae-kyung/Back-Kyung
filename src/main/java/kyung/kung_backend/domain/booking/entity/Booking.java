package kyung.kung_backend.domain.booking.entity;

import jakarta.persistence.*;
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

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;

    @Column(name = "CANCELLED_AT")
    private LocalDateTime cancelledAt;
}