package kyung.kung_backend.domain.booking.dto;

import kyung.kung_backend.domain.booking.entity.Booking;
import kyung.kung_backend.domain.coupon.dto.AvailableCouponDto;
import kyung.kung_backend.domain.expert.entity.ExpertProfile;
import kyung.kung_backend.domain.location.entity.Location;
import kyung.kung_backend.domain.store.entity.StoreProduct;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class BookingResponse {

    private Long bookingId;
    private Long userId;
    private Long storeProductId;
    private Long expertProfileId;

    private String productTitle;
    private String expertDisplayName;

    private LocalDateTime startAt;
    private LocalDateTime endAt;

    private Long locationId;
    private String locationName;
    private String locationText;

    private String status;
    private LocalDateTime paymentExpiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 추가된 쿠폰 정보 필드
    private String welcomeCouponAvailable;
    private List<AvailableCouponDto> availableCoupons;

    public static BookingResponse from(Booking booking) {
        return of(booking, null, null);
    }

    public static BookingResponse of(
            Booking booking,
            String welcomeCouponAvailable,
            List<AvailableCouponDto> availableCoupons
    ) {
        StoreProduct storeProduct = booking.getStoreProduct();
        ExpertProfile expertProfile =
                storeProduct != null
                        ? storeProduct.getExpertProfile()
                        : null;
        Location location = booking.getLocation();

        return BookingResponse.builder()
                .bookingId(booking.getBookingId())
                .userId(booking.getUser().getUserId())
                .storeProductId(
                        storeProduct != null
                                ? storeProduct.getStoreProductId()
                                : null
                )
                .expertProfileId(
                        expertProfile != null
                                ? expertProfile.getExpertProfileId()
                                : null
                )
                .productTitle(
                        storeProduct != null
                                ? storeProduct.getTitle()
                                : null
                )
                .expertDisplayName(
                        expertProfile != null
                                ? expertProfile.getDisplayName()
                                : null
                )
                .startAt(booking.getStartAt())
                .endAt(booking.getEndAt())
                .locationId(
                        location != null
                                ? location.getLocationId()
                                : null
                )
                .locationName(
                        location != null
                                ? location.getName()
                                : null
                )
                .locationText(booking.getLocationText())
                .status(booking.getStatus())
                .paymentExpiresAt(booking.getPaymentExpiresAt())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .welcomeCouponAvailable(welcomeCouponAvailable)
                .availableCoupons(availableCoupons)
                .build();
    }
}