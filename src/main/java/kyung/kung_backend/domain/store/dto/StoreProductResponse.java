package kyung.kung_backend.domain.store.dto;

import kyung.kung_backend.domain.store.entity.StoreProduct;
import kyung.kung_backend.domain.store.entity.enums.StoreProductServiceType;
import kyung.kung_backend.domain.store.entity.enums.StoreProductStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class StoreProductResponse {

    private Long storeProductId;

    private Long expertProfileId;

    private Long categoryId;

    private String categoryName;

    private String title;

    private String thumbnailImageUrl;

    private String description;

    private BigDecimal price;

    private StoreProductServiceType serviceType;

    private String serviceRegion;

    private StoreProductStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static StoreProductResponse from(StoreProduct storeProduct) {
        return StoreProductResponse.builder()
                .storeProductId(storeProduct.getStoreProductId())
                .expertProfileId(storeProduct.getExpertProfile().getExpertProfileId())
                .categoryId(storeProduct.getCategory().getCategoryId())
                .categoryName(storeProduct.getCategory().getName())
                .title(storeProduct.getTitle())
                .thumbnailImageUrl(storeProduct.getThumbnailImageUrl())
                .description(storeProduct.getDescription())
                .price(storeProduct.getPrice())
                .serviceType(storeProduct.getServiceType())
                .serviceRegion(storeProduct.getServiceRegion())
                .status(storeProduct.getStatus())
                .createdAt(storeProduct.getCreatedAt())
                .updatedAt(storeProduct.getUpdatedAt())
                .build();
    }
}