package kyung.kung_backend.domain.transaction.entity;

import jakarta.persistence.*;
import kyung.kung_backend.domain.booking.entity.Booking;
import kyung.kung_backend.domain.purchase.entity.Purchase;
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
                @UniqueConstraint(name = "UK_TRANSACTIONS_PURCHASE", columnNames = "PURCHASE_ID"),
                @UniqueConstraint(name = "UK_TRANSACTIONS_ORDER_ID", columnNames = "ORDER_ID")
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

    /*
     * PG사에 넘기는 주문 번호입니다.
     * 프론트는 결제 위젯을 띄울 때 이 값을 사용하고, 결제 승인 API는 이 값으로
     * 서버에 저장된 거래와 PG가 돌려준 결제 결과를 다시 매칭합니다.
     */
    @Column(name = "ORDER_ID", nullable = false, unique = true, length = 100)
    private String orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BOOKING_ID")
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

    public static final String TYPE_BOOKING = "BOOKING";
    public static final String TYPE_PURCHASE = "PURCHASE";

    public static final String STATUS_READY = "READY";
    public static final String STATUS_PAID = "PAID";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_REFUNDED = "REFUNDED";

    public static Transaction createForBooking(
            Booking booking,
            User buyer,
            User seller,
            String orderId,
            BigDecimal totalAmount,
            BigDecimal discountAmount,
            BigDecimal finalAmount
    ) {
        Transaction transaction = new Transaction();

        transaction.orderId = orderId;
        transaction.booking = booking;
        transaction.purchase = null;
        transaction.buyer = buyer;
        transaction.seller = seller;
        transaction.transactionType = TYPE_BOOKING;
        transaction.totalAmount = totalAmount;
        transaction.discountAmount = discountAmount;
        transaction.finalAmount = finalAmount;
        transaction.status = STATUS_READY;

        return transaction;
    }

    public boolean isReady() {
        return STATUS_READY.equals(this.status);
    }

    public boolean isPaid() {
        return STATUS_PAID.equals(this.status);
    }

    public void markPaid() {
        this.status = STATUS_PAID;
    }

    public void markFailed() {
        this.status = STATUS_FAILED;
    }

    public void markCancelled() {
        this.status = STATUS_CANCELLED;
    }

    public void markRefunded() {
        this.status = STATUS_REFUNDED;
    }
}
