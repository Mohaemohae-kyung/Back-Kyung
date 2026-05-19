package kyung.kung_backend.domain.payment.dto;

import kyung.kung_backend.domain.booking.entity.Booking;
import kyung.kung_backend.domain.expert.entity.ExpertProfile;
import kyung.kung_backend.domain.store.entity.StoreProduct;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class BookingCheckoutResponse {

    private Long bookingId;
    private Long storeProductId;
    private Long expertServiceId;
    private String productTitle;
    private String serviceTitle;
    private String expertDisplayName;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String locationText;
    private String bookingStatus;
    private LocalDateTime paymentExpiresAt;
    private BigDecimal baseAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;

    public static BookingCheckoutResponse of(
            Booking booking,
            BigDecimal baseAmount,
            BigDecimal discountAmount,
            BigDecimal finalAmount
    ) {
        StoreProduct storeProduct = booking.getStoreProduct();
        ExpertProfile expertProfile = storeProduct != null ? storeProduct.getExpertProfile() : null;

        return BookingCheckoutResponse.builder()
                .bookingId(booking.getBookingId())
                .storeProductId(storeProduct != null ? storeProduct.getStoreProductId() : null)
                .expertServiceId(null)
                .productTitle(storeProduct != null ? storeProduct.getTitle() : null)
                .serviceTitle(null)
                .expertDisplayName(expertProfile != null ? expertProfile.getDisplayName() : null)
                .startAt(booking.getStartAt())
                .endAt(booking.getEndAt())
                .locationText(booking.getLocationText())
                .bookingStatus(booking.getStatus())
                .paymentExpiresAt(booking.getPaymentExpiresAt())
                .baseAmount(baseAmount)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .build();
    }
}
