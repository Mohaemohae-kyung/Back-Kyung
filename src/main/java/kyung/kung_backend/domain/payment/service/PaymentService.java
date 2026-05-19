package kyung.kung_backend.domain.payment.service;

import kyung.kung_backend.domain.booking.entity.Booking;
import kyung.kung_backend.domain.booking.service.BookingService;
import kyung.kung_backend.domain.coupon.entity.Coupon;
import kyung.kung_backend.domain.coupon.entity.UserCoupon;
import kyung.kung_backend.domain.coupon.repository.UserCouponRepository;
import kyung.kung_backend.domain.payment.dto.BookingCheckoutResponse;
import kyung.kung_backend.domain.payment.dto.PaymentCancelRequest;
import kyung.kung_backend.domain.payment.dto.PaymentConfirmRequest;
import kyung.kung_backend.domain.payment.dto.PaymentPrepareRequest;
import kyung.kung_backend.domain.payment.dto.PaymentPrepareResponse;
import kyung.kung_backend.domain.payment.dto.PaymentResponse;
import kyung.kung_backend.domain.payment.entity.Payment;
import kyung.kung_backend.domain.payment.repository.PaymentRepository;
import kyung.kung_backend.domain.servicepost.entity.ExpertService;
import kyung.kung_backend.domain.transaction.entity.Transaction;
import kyung.kung_backend.domain.transaction.repository.TransactionRepository;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.domain.user.repository.UserRepository;
import kyung.kung_backend.global.exception.GeneralException;
import kyung.kung_backend.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private static final String TARGET_TYPE_BOOKING = "BOOKING";
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final DateTimeFormatter ORDER_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final BookingService bookingService;
    private final TransactionRepository transactionRepository;
    private final PaymentRepository paymentRepository;
    private final UserCouponRepository userCouponRepository;
    private final UserRepository userRepository;

    @Transactional
    public BookingCheckoutResponse getBookingCheckout(
            User loginUser,
            Long bookingId
    ) {
        Booking booking = bookingService.findOwnedBooking(loginUser, bookingId);
        validateBookingPayable(booking, LocalDateTime.now());

        BigDecimal baseAmount = getBookingBaseAmount(booking);

        /*
         * 결제 화면 최초 진입 응답에서는 쿠폰이 적용되지 않은 기본 금액을 내려줍니다.
         * 실제 쿠폰 적용 금액은 payments/prepare 요청에 userCouponId가 들어왔을 때 다시 계산합니다.
         */
        return BookingCheckoutResponse.of(booking, baseAmount, ZERO, baseAmount);
    }

    @Transactional
    public PaymentPrepareResponse preparePayment(
            User loginUser,
            PaymentPrepareRequest request
    ) {
        User user = findLoginUser(loginUser);

        if (!TARGET_TYPE_BOOKING.equals(request.getTargetType())) {
            throw GeneralException.of(ErrorCode.PAYMENT_UNSUPPORTED_TARGET);
        }

        LocalDateTime now = LocalDateTime.now();

        /*
         * 결제 준비 시점에도 만료된 임시 예약을 정리합니다.
         * 사용자가 예약 화면을 오래 열어 둔 뒤 결제를 시도하는 케이스를 여기서 한 번 더 차단합니다.
         */
        bookingService.expireStalePendingBookings(now);

        Booking booking = bookingService.findOwnedBooking(user, request.getTargetId());
        validateBookingPayable(booking, now);

        /*
         * 동일 예약에 대해 이미 READY 결제 건이 있으면 새 결제 건을 만들지 않고 기존 주문 정보를 돌려줍니다.
         * 프론트 새로고침이나 중복 클릭으로 주문이 여러 개 생성되는 것을 막기 위한 장치입니다.
         */
        Payment existingReadyPayment = findExistingReadyPayment(booking);
        if (existingReadyPayment != null) {
            if (samePrepareCondition(existingReadyPayment, request)) {
                return PaymentPrepareResponse.of(
                        existingReadyPayment,
                        existingReadyPayment.getTransaction(),
                        getOrderName(booking)
                );
            }

            /*
             * 쿠폰이나 결제수단이 바뀐 경우 기존 READY 주문을 그대로 쓰면 금액/결제 조건이 어긋납니다.
             * 아직 PG 승인 전 상태이므로 기존 준비 건은 취소하고 새 orderId로 다시 생성합니다.
             */
            existingReadyPayment.cancel("결제 조건 변경으로 기존 결제 준비 건을 취소합니다.");
            existingReadyPayment.getTransaction().markCancelled();
        }

        BigDecimal totalAmount = getBookingBaseAmount(booking);
        UserCoupon userCoupon = findUsableCoupon(request.getUserCouponId(), user, now);
        BigDecimal discountAmount = calculateDiscountAmount(userCoupon, totalAmount);
        BigDecimal finalAmount = totalAmount.subtract(discountAmount);

        String orderId = createOrderId(booking.getBookingId(), now);
        User seller = booking.getExpertProfile() != null ? booking.getExpertProfile().getUser() : null;

        Transaction transaction = Transaction.createForBooking(
                booking,
                user,
                seller,
                orderId,
                totalAmount,
                discountAmount,
                finalAmount
        );

        Transaction savedTransaction = transactionRepository.save(transaction);

        Payment payment = Payment.createReady(
                savedTransaction,
                user,
                userCoupon,
                request.getPaymentMethod(),
                finalAmount,
                request.getPgProvider()
        );

        Payment savedPayment = paymentRepository.save(payment);

        return PaymentPrepareResponse.of(savedPayment, savedTransaction, getOrderName(booking));
    }

    @Transactional
    public PaymentResponse confirmPayment(PaymentConfirmRequest request) {
        Payment payment = paymentRepository.findByTransactionOrderId(request.getOrderId())
                .orElseThrow(() -> GeneralException.of(ErrorCode.PAYMENT_NOT_FOUND));

        Transaction transaction = payment.getTransaction();

        /*
         * 같은 orderId/paymentKey로 confirm이 재호출되면 이미 성공한 결제 응답을 그대로 돌려줍니다.
         * 모바일 환경에서 네트워크 재시도나 프론트 중복 요청이 발생할 수 있기 때문입니다.
         */
        if (payment.isPaid()) {
            if (request.getPaymentKey().equals(payment.getPgPaymentKey())
                    && sameAmount(request.getAmount(), transaction.getFinalAmount())) {
                return PaymentResponse.from(payment);
            }

            throw GeneralException.of(ErrorCode.PAYMENT_INVALID_STATUS);
        }

        if (!payment.isReady() || !transaction.isReady()) {
            throw GeneralException.of(ErrorCode.PAYMENT_INVALID_STATUS);
        }

        if (!sameAmount(request.getAmount(), transaction.getFinalAmount())) {
            payment.fail("결제 승인 요청 금액과 서버 주문 금액이 일치하지 않습니다.");
            transaction.markFailed();
            throw GeneralException.of(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        validatePgPaymentKeyNotUsed(request.getPaymentKey(), payment);

        /*
         * 실제 PG 연동 위치:
         * 운영 코드에서는 여기서 paymentKey, orderId, amount를 PG 서버에 전달해 승인(confirm) API를 호출해야 합니다.
         * PG 승인 성공 응답을 받은 뒤에만 아래처럼 Payment/Transaction/Booking 상태를 완료로 바꿔야 합니다.
         *
         * 현재 구현은 API 구조를 먼저 만들기 위한 로컬 승인 처리입니다.
         */
        LocalDateTime paidAt = LocalDateTime.now();
        payment.complete(request.getPaymentKey(), paidAt);
        transaction.markPaid();
        transaction.getBooking().confirmPayment();

        if (payment.getUserCoupon() != null) {
            payment.getUserCoupon().use(paidAt);
        }

        return PaymentResponse.from(payment);
    }

    @Transactional
    public PaymentResponse cancelPayment(
            User loginUser,
            Long paymentId,
            PaymentCancelRequest request
    ) {
        User user = findLoginUser(loginUser);
        Payment payment = paymentRepository.findByPaymentIdAndUserUserId(paymentId, user.getUserId())
                .orElseThrow(() -> GeneralException.of(ErrorCode.PAYMENT_NOT_FOUND));

        Transaction transaction = payment.getTransaction();
        String reason = request.getReason() == null || request.getReason().isBlank()
                ? "사용자 요청"
                : request.getReason();

        /*
         * READY 상태는 아직 PG 승인 전이므로 서버 상태만 취소하면 됩니다.
         * PAID 상태는 실제 운영에서 PG 결제 취소/환불 API 호출이 먼저 성공해야 합니다.
         */
        if (payment.isReady()) {
            payment.cancel(reason);
            transaction.markCancelled();
            transaction.getBooking().cancel();
            return PaymentResponse.from(payment);
        }

        if (payment.isPaid()) {
            payment.refund(reason);
            transaction.markRefunded();
            transaction.getBooking().cancel();

            if (payment.getUserCoupon() != null) {
                payment.getUserCoupon().restoreAfterPaymentCancel();
            }

            return PaymentResponse.from(payment);
        }

        throw GeneralException.of(ErrorCode.PAYMENT_INVALID_STATUS);
    }

    public PaymentResponse getPaymentDetail(
            User loginUser,
            Long paymentId
    ) {
        User user = findLoginUser(loginUser);

        Payment payment = paymentRepository.findByPaymentIdAndUserUserId(paymentId, user.getUserId())
                .orElseThrow(() -> GeneralException.of(ErrorCode.PAYMENT_NOT_FOUND));

        return PaymentResponse.from(payment);
    }

    public List<PaymentResponse> getMyPayments(User loginUser) {
        User user = findLoginUser(loginUser);

        return paymentRepository.findAllByUserUserIdOrderByCreatedAtDesc(user.getUserId())
                .stream()
                .map(PaymentResponse::from)
                .toList();
    }

    private User findLoginUser(User loginUser) {
        if (loginUser == null || loginUser.getUserId() == null) {
            throw GeneralException.of(ErrorCode.UNAUTHORIZED);
        }

        return userRepository.findById(loginUser.getUserId())
                .orElseThrow(() -> GeneralException.of(ErrorCode.UNAUTHORIZED));
    }

    private void validateBookingPayable(
            Booking booking,
            LocalDateTime now
    ) {
        if (!booking.isPendingPayment()) {
            throw GeneralException.of(ErrorCode.BOOKING_NOT_PAYABLE);
        }

        if (booking.isPaymentExpired(now)) {
            booking.expirePayment();
            throw GeneralException.of(ErrorCode.BOOKING_NOT_PAYABLE);
        }
    }

    private BigDecimal getBookingBaseAmount(Booking booking) {
        ExpertService expertService = booking.getExpertService();

        if (expertService == null || expertService.getBasePrice() == null) {
            throw GeneralException.of(ErrorCode.BOOKING_NOT_PAYABLE);
        }

        return expertService.getBasePrice();
    }

    private Payment findExistingReadyPayment(Booking booking) {
        return transactionRepository
                .findFirstByBookingBookingIdAndStatusOrderByCreatedAtDesc(
                        booking.getBookingId(),
                        Transaction.STATUS_READY
                )
                .flatMap(transaction -> paymentRepository.findByTransactionOrderId(transaction.getOrderId()))
                .filter(Payment::isReady)
                .orElse(null);
    }

    private boolean samePrepareCondition(
            Payment payment,
            PaymentPrepareRequest request
    ) {
        Long savedCouponId = payment.getUserCoupon() != null
                ? payment.getUserCoupon().getUserCouponId()
                : null;

        return Objects.equals(savedCouponId, request.getUserCouponId())
                && Objects.equals(payment.getPaymentMethod(), request.getPaymentMethod())
                && Objects.equals(payment.getPgProvider(), request.getPgProvider());
    }

    private UserCoupon findUsableCoupon(
            Long userCouponId,
            User user,
            LocalDateTime now
    ) {
        if (userCouponId == null) {
            return null;
        }

        UserCoupon userCoupon = userCouponRepository
                .findByUserCouponIdAndUserUserId(userCouponId, user.getUserId())
                .orElseThrow(() -> GeneralException.of(ErrorCode.COUPON_NOT_AVAILABLE));

        if (!userCoupon.isUsable(now)) {
            throw GeneralException.of(ErrorCode.COUPON_NOT_AVAILABLE);
        }

        return userCoupon;
    }

    private BigDecimal calculateDiscountAmount(
            UserCoupon userCoupon,
            BigDecimal totalAmount
    ) {
        if (userCoupon == null) {
            return ZERO;
        }

        Coupon coupon = userCoupon.getCoupon();

        if (coupon.getMinOrderAmount() != null && totalAmount.compareTo(coupon.getMinOrderAmount()) < 0) {
            throw GeneralException.of(ErrorCode.COUPON_NOT_AVAILABLE);
        }

        BigDecimal discountAmount = ZERO;

        if (Coupon.DISCOUNT_TYPE_FIXED.equals(coupon.getDiscountType()) && coupon.getDiscountAmount() != null) {
            discountAmount = coupon.getDiscountAmount();
        }

        if (Coupon.DISCOUNT_TYPE_RATE.equals(coupon.getDiscountType()) && coupon.getDiscountRate() != null) {
            discountAmount = totalAmount
                    .multiply(BigDecimal.valueOf(coupon.getDiscountRate()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.DOWN);
        }

        if (coupon.getMaxDiscountAmount() != null) {
            discountAmount = discountAmount.min(coupon.getMaxDiscountAmount());
        }

        return discountAmount.min(totalAmount);
    }

    private void validatePgPaymentKeyNotUsed(
            String pgPaymentKey,
            Payment currentPayment
    ) {
        paymentRepository.findByPgPaymentKey(pgPaymentKey)
                .filter(payment -> !payment.getPaymentId().equals(currentPayment.getPaymentId()))
                .ifPresent(payment -> {
                    throw GeneralException.of(ErrorCode.PAYMENT_DUPLICATE_KEY);
                });
    }

    private boolean sameAmount(
            BigDecimal requestedAmount,
            BigDecimal savedAmount
    ) {
        return requestedAmount != null && requestedAmount.compareTo(savedAmount) == 0;
    }

    private String createOrderId(
            Long bookingId,
            LocalDateTime now
    ) {
        String shortUuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return "BOOKING-" + bookingId + "-" + now.format(ORDER_TIME_FORMATTER) + "-" + shortUuid;
    }

    private String getOrderName(Booking booking) {
        ExpertService expertService = booking.getExpertService();

        if (expertService == null || expertService.getTitle() == null || expertService.getTitle().isBlank()) {
            return "예약 결제";
        }

        return expertService.getTitle();
    }
}
