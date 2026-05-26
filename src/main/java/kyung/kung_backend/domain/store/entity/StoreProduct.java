package kyung.kung_backend.domain.store.entity;

import jakarta.persistence.*;
import kyung.kung_backend.domain.category.entity.ServiceCategory;
import kyung.kung_backend.domain.expert.entity.ExpertProfile;
import kyung.kung_backend.domain.location.entity.Location;
import kyung.kung_backend.domain.store.entity.enums.StoreProductServiceType;
import kyung.kung_backend.domain.store.entity.enums.StoreProductStatus;
import kyung.kung_backend.global.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "STORE_PRODUCTS")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "STORE_PRODUCTS_SEQ_GENERATOR",
        sequenceName = "STORE_PRODUCTS_SEQ",
        allocationSize = 1
)
public class StoreProduct extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STORE_PRODUCTS_SEQ_GENERATOR")
    @Column(name = "STORE_PRODUCT_ID", nullable = false)
    private Long storeProductId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "EXPERT_PROFILE_ID", nullable = false)
    private ExpertProfile expertProfile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CATEGORY_ID", nullable = false)
    private ServiceCategory category;

    @Column(name = "TITLE", nullable = false, length = 200)
    private String title;

    @Column(name = "THUMBNAIL_IMAGE_URL", nullable = false, length = 500)
    private String thumbnailImageUrl;

    @Lob
    @Column(name = "DESCRIPTION", nullable = false)
    private String description;

    @Column(name = "PRICE", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "SERVICE_TYPE", nullable = false, length = 20)
    private StoreProductServiceType serviceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LOCATION_ID")
    private Location location;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private StoreProductStatus status;

    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    @Builder
    private StoreProduct(
            ExpertProfile expertProfile,
            ServiceCategory category,
            String title,
            String thumbnailImageUrl,
            String description,
            BigDecimal price,
            StoreProductServiceType serviceType,
            Location location,
            StoreProductStatus status
    ) {
        this.expertProfile = expertProfile;
        this.category = category;
        this.title = title;
        this.thumbnailImageUrl = thumbnailImageUrl;
        this.description = description;
        this.price = price;
        this.serviceType = serviceType;
        this.location = location;
        this.status = status;
    }

    public void update(
            ServiceCategory category,
            String title,
            String thumbnailImageUrl,
            String description,
            BigDecimal price,
            StoreProductServiceType serviceType,
            Location location
    ) {
        if (category != null) {
            this.category = category;
        }

        if (title != null) {
            this.title = title;
        }

        if (thumbnailImageUrl != null) {
            this.thumbnailImageUrl = thumbnailImageUrl;
        }

        if (description != null) {
            this.description = description;
        }

        if (price != null) {
            this.price = price;
        }

        if (serviceType != null) {
            this.serviceType = serviceType;
        }

        if (location != null) {
            this.location = location;
        }
    }

    public void delete() {
        this.status = StoreProductStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }

    public void hide() {
        this.status = StoreProductStatus.HIDDEN;
    }

    public void clearLocation() {
        this.location = null;
    }

    public void activate() {
        this.status = StoreProductStatus.ACTIVE;
        this.deletedAt = null;
    }
}
