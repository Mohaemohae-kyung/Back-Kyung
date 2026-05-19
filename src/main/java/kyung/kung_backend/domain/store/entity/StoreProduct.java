package kyung.kung_backend.domain.store.entity;

import jakarta.persistence.*;
import kyung.kung_backend.domain.category.entity.ServiceCategory;
import kyung.kung_backend.domain.expert.entity.ExpertProfile;
import kyung.kung_backend.domain.store.entity.enums.StoreProductServiceType;
import kyung.kung_backend.domain.store.entity.enums.StoreProductStatus;
import kyung.kung_backend.global.common.BaseEntity;
import lombok.AccessLevel;
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

    @Column(name = "SERVICE_REGION", length = 100)
    private String serviceRegion;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private StoreProductStatus status;

    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;
}