package kyung.kung_backend.domain.purchase.entity;

import jakarta.persistence.*;
import kyung.kung_backend.domain.store.entity.StoreProduct;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "PURCHASES")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "PURCHASES_SEQ_GENERATOR",
        sequenceName = "PURCHASES_SEQ",
        allocationSize = 1
)
public class Purchase extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PURCHASES_SEQ_GENERATOR")
    @Column(name = "PURCHASE_ID", nullable = false)
    private Long purchaseId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "STORE_PRODUCT_ID", nullable = false)
    private StoreProduct storeProduct;

    @Column(name = "QUANTITY", nullable = false)
    private Long quantity;

    @Column(name = "TOTAL_PRICE", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;
}