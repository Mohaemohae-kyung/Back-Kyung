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

    /**
     * 서비스 예약 결제를 위한 거래 단위를 생성합니다.
     *
     * 호출 위치:
     * - PaymentService.prepare()에서 Payment를 만들기 전에 먼저 호출합니다.
     *
     * 사용 목적:
     * - 결제는 단독으로 존재하지 않고 "무엇을 결제하는지"를 나타내는 거래(Transaction)에 연결됩니다.
     * - 현재 DB 구조상 Transaction은 Booking 또는 Purchase에 연결될 수 있습니다.
     * - prepare API는 서비스 예약 결제를 대상으로 하므로 booking을 우선 연결합니다.
     *
     * 참고:
     * - 현재 prepare API는 bookingId를 필수로 사용하므로 booking은 null이면 안 됩니다.
     * - 추후 matchId만으로 결제를 시작하는 정책을 선택한다면,
     *   결제 전 Booking을 먼저 생성하거나 Transaction에 Match 연결 필드를 추가해야 합니다.
     */
    public static Transaction prepareServiceBookingPayment(
            Booking booking,
            User buyer,
            User seller,
            BigDecimal amount,
            String status
    ) {
        Transaction transaction = new Transaction();
        transaction.booking = booking;
        transaction.buyer = buyer;
        transaction.seller = seller;
        transaction.transactionType = "SERVICE_BOOKING";
        transaction.totalAmount = amount;
        transaction.discountAmount = BigDecimal.ZERO;
        transaction.finalAmount = amount;
        transaction.status = status;
        return transaction;
    }
}
