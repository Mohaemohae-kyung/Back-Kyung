package kyung.kung_backend.domain.transaction.entity;

import jakarta.persistence.*;
import kyung.kung_backend.domain.booking.entity.Booking;
import kyung.kung_backend.domain.purchase.entity.Purchase;
import kyung.kung_backend.domain.request.entity.ServiceRequest;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@Table(
        name = "TRANSACTIONS",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_TRANSACTIONS_REQUEST", columnNames = "REQUEST_ID"),
                @UniqueConstraint(name = "UK_TRANSACTIONS_BOOKING", columnNames = "BOOKING_ID"),
                @UniqueConstraint(name = "UK_TRANSACTIONS_PURCHASE", columnNames = "PURCHASE_ID")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "TRANSACTIONS_SEQ_GENERATOR",
        sequenceName = "TRANSACTIONS_SEQ",
        allocationSize = 1
)
public class Transaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TRANSACTIONS_SEQ_GENERATOR")
    @Column(name = "TRANSACTION_ID", nullable = false)
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REQUEST_ID", unique = true)
    private ServiceRequest serviceRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BOOKING_ID", unique = true)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PURCHASE_ID", unique = true)
    private Purchase purchase;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "BUYER_ID", nullable = false)
    private User buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SELLER_ID")
    private User seller;

    @Column(name = "TRANSACTION_TYPE", nullable = false, length = 30)
    private String transactionType;

    @Column(name = "TOTAL_AMOUNT", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "DISCOUNT_AMOUNT", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "FINAL_AMOUNT", nullable = false, precision = 12, scale = 2)
    private BigDecimal finalAmount;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;
}