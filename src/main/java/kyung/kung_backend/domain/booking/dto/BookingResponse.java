package kyung.kung_backend.domain.booking.dto;

import kyung.kung_backend.domain.booking.entity.Booking;
import kyung.kung_backend.domain.expert.entity.ExpertProfile;
import kyung.kung_backend.domain.servicepost.entity.ExpertService;
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

    /*
     * Entity를 API 응답으로 바꾸는 변환 메서드입니다.
     * 컨트롤러가 JPA Entity를 그대로 노출하지 않도록 DTO에서 필요한 값만 골라 내려줍니다.
     */
    public static BookingResponse from(Booking booking) {
        ExpertService expertService = booking.getExpertService();
        StoreProduct storeProduct = booking.getStoreProduct();
        ExpertProfile expertProfile = booking.getExpertProfile();

        return BookingResponse.builder()
                .bookingId(booking.getBookingId())
                .userId(booking.getUser().getUserId())
                .storeProductId(storeProduct != null ? storeProduct.getStoreProductId() : null)
                .expertServiceId(expertService != null ? expertService.getExpertServiceId() : null)
                .expertProfileId(expertProfile != null ? expertProfile.getExpertProfileId() : null)
                .productTitle(storeProduct != null ? storeProduct.getTitle() : null)
                .serviceTitle(expertService != null ? expertService.getTitle() : null)
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
