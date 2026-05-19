package kyung.kung_backend.domain.booking.dto;

import kyung.kung_backend.domain.booking.entity.Booking;
import kyung.kung_backend.domain.expert.entity.ExpertProfile;
import kyung.kung_backend.domain.store.entity.StoreProduct;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BookingResponse {

    private Long bookingId;
    private Long userId;
    private Long storeProductId;
    private Long expertServiceId;
    private Long expertProfileId;
    private String productTitle;
    private String serviceTitle;
    private String expertDisplayName;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String locationText;
    private String status;
    private LocalDateTime paymentExpiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BookingResponse from(Booking booking) {
        StoreProduct storeProduct = booking.getStoreProduct();
        ExpertProfile expertProfile = storeProduct != null ? storeProduct.getExpertProfile() : null;

        return BookingResponse.builder()
                .bookingId(booking.getBookingId())
                .userId(booking.getUser().getUserId())
                .storeProductId(storeProduct != null ? storeProduct.getStoreProductId() : null)
                .expertServiceId(null)
                .expertProfileId(expertProfile != null ? expertProfile.getExpertProfileId() : null)
                .productTitle(storeProduct != null ? storeProduct.getTitle() : null)
                .serviceTitle(null)
                .expertDisplayName(expertProfile != null ? expertProfile.getDisplayName() : null)
                .startAt(booking.getStartAt())
                .endAt(booking.getEndAt())
                .locationText(booking.getLocationText())
                .status(booking.getStatus())
                .paymentExpiresAt(booking.getPaymentExpiresAt())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}
