package kyung.kung_backend.domain.transaction.entity;

import jakarta.persistence.*;
import kyung.kung_backend.domain.booking.entity.Booking;
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
                @UniqueConstraint(name = "UK_TRANSACTIONS_BOOKING", columnNames = "BOOKING_ID")
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

    @Column(name = "ORDER_ID", nullable = false, unique = true, length = 100)
    private String orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REQUEST_ID", unique = true)
    private ServiceRequest serviceRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BOOKING_ID", unique = true)
    private Booking booking;

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
    public static final String TYPE_SERVICE_REQUEST = "SERVICE_REQUEST";
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
        transaction.resetForBooking(booking, buyer, seller, orderId, totalAmount, discountAmount, finalAmount);
        return transaction;
    }

    public static Transaction createForServiceRequest(
            ServiceRequest serviceRequest,
            User buyer,
            User seller,
            String orderId,
            BigDecimal totalAmount,
            BigDecimal discountAmount,
            BigDecimal finalAmount
    ) {
        Transaction transaction = new Transaction();
        transaction.resetForServiceRequest(serviceRequest, buyer, seller, orderId, totalAmount, discountAmount, finalAmount);
        return transaction;
    }

    public void resetForBooking(
            Booking booking,
            User buyer,
            User seller,
            String orderId,
            BigDecimal totalAmount,
            BigDecimal discountAmount,
            BigDecimal finalAmount
    ) {
        this.orderId = orderId;
        this.booking = booking;
        this.serviceRequest = null;
        this.purchase = null;
        this.buyer = buyer;
        this.seller = seller;
        this.transactionType = TYPE_BOOKING;
        this.totalAmount = totalAmount;
        this.discountAmount = discountAmount;
        this.finalAmount = finalAmount;
        this.status = STATUS_READY;
    }

    public void resetForServiceRequest(
            ServiceRequest serviceRequest,
            User buyer,
            User seller,
            String orderId,
            BigDecimal totalAmount,
            BigDecimal discountAmount,
            BigDecimal finalAmount
    ) {
        this.orderId = orderId;
        this.booking = null;
        this.serviceRequest = serviceRequest;
        this.purchase = null;
        this.buyer = buyer;
        this.seller = seller;
        this.transactionType = TYPE_SERVICE_REQUEST;
        this.totalAmount = totalAmount;
        this.discountAmount = discountAmount;
        this.finalAmount = finalAmount;
        this.status = STATUS_READY;
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
