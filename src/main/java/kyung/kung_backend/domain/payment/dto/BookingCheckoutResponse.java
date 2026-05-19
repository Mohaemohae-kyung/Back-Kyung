package kyung.kung_backend.domain.payment.dto;

import kyung.kung_backend.domain.booking.entity.Booking;
import kyung.kung_backend.domain.expert.entity.ExpertProfile;
import kyung.kung_backend.domain.servicepost.entity.ExpertService;
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

    /*
     * 결제 화면 진입 시 내려주는 요약 응답입니다.
     * 프론트는 이 응답을 보여주기만 하고, 실제 결제 금액은 payments/prepare에서 서버가 다시 계산합니다.
     */
    public static BookingCheckoutResponse of(
            Booking booking,
            BigDecimal baseAmount,
            BigDecimal discountAmount,
            BigDecimal finalAmount
    ) {
        ExpertService expertService = booking.getExpertService();
        StoreProduct storeProduct = booking.getStoreProduct();
        ExpertProfile expertProfile = booking.getExpertProfile();

        return BookingCheckoutResponse.builder()
                .bookingId(booking.getBookingId())
                .storeProductId(storeProduct != null ? storeProduct.getStoreProductId() : null)
                .expertServiceId(expertService != null ? expertService.getExpertServiceId() : null)
                .productTitle(storeProduct != null ? storeProduct.getTitle() : null)
                .serviceTitle(expertService != null ? expertService.getTitle() : null)
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
