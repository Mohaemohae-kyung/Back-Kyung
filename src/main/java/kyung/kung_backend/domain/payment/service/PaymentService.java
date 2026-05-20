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
import kyung.kung_backend.domain.payment.dto.ServiceRequestCheckoutResponse;
import kyung.kung_backend.domain.payment.entity.Payment;
import kyung.kung_backend.domain.payment.repository.PaymentRepository;
import kyung.kung_backend.domain.request.entity.ServiceRequest;
import kyung.kung_backend.domain.request.repository.ServiceRequestRepository;
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
    private static final String TARGET_TYPE_SERVICE_REQUEST = "SERVICE_REQUEST";
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final DateTimeFormatter ORDER_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final BookingService bookingService;
    private final TransactionRepository transactionRepository;
    private final PaymentRepository paymentRepository;
    private final UserCouponRepository userCouponRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final UserRepository userRepository;

    @Transactional
    public BookingCheckoutResponse getBookingCheckout(
            User loginUser,
            Long bookingId
    ) {
        Booking booking = bookingService.findOwnedBooking(loginUser, bookingId);
        validateBookingPayable(booking, LocalDateTime.now());

        BigDecimal baseAmount = getBookingBaseAmount(booking);
        return BookingCheckoutResponse.of(booking, baseAmount, ZERO, baseAmount);
    }

    @Transactional
    public ServiceRequestCheckoutResponse getServiceRequestCheckout(
            User loginUser,
            Long requestId
    ) {
        User user = findLoginUser(loginUser);
        ServiceRequest serviceRequest = findOwnedServiceRequest(user, requestId);
        validateServiceRequestPayable(serviceRequest);

        BigDecimal baseAmount = getServiceRequestAmount(serviceRequest);
        return ServiceRequestCheckoutResponse.of(serviceRequest, baseAmount, ZERO, baseAmount);
    }

    @Transactional
    public PaymentPrepareResponse preparePayment(
            User loginUser,
            PaymentPrepareRequest request
    ) {
        User user = findLoginUser(loginUser);
        LocalDateTime now = LocalDateTime.now();

        if (TARGET_TYPE_BOOKING.equals(request.getTargetType())) {
            return prepareBookingPayment(user, request, now);
        }

        if (TARGET_TYPE_SERVICE_REQUEST.equals(request.getTargetType())) {
            return prepareServiceRequestPayment(user, request, now);
        }

        throw GeneralException.of(ErrorCode.PAYMENT_UNSUPPORTED_TARGET);
    }

    private PaymentPrepareResponse prepareBookingPayment(
            User user,
            PaymentPrepareRequest request,
            LocalDateTime now
    ) {
        bookingService.expireStalePendingBookings(now);

        Booking booking = bookingService.findOwnedBooking(user, request.getTargetId());
        validateBookingPayable(booking, now);

        Payment existingReadyPayment = findExistingReadyPayment(booking);
        if (existingReadyPayment != null && samePrepareCondition(existingReadyPayment, request)) {
            return PaymentPrepareResponse.of(
                    existingReadyPayment,
                    existingReadyPayment.getTransaction(),
                    getOrderName(booking)
            );
        }

        BigDecimal totalAmount = getBookingBaseAmount(booking);
        UserCoupon userCoupon = findUsableCoupon(request.getUserCouponId(), user, now);
        BigDecimal discountAmount = calculateDiscountAmount(userCoupon, totalAmount);
        BigDecimal finalAmount = totalAmount.subtract(discountAmount);
        String orderId = createOrderId("BOOKING", booking.getBookingId(), now);
        User seller = resolveBookingSeller(booking);

        Transaction transaction = prepareBookingTransaction(
                booking,
                user,
                seller,
                orderId,
                totalAmount,
                discountAmount,
                finalAmount
        );

        Payment payment = prepareReadyPayment(
                transaction,
                user,
                userCoupon,
                request.getPaymentMethod(),
                finalAmount,
                request.getPgProvider()
        );

        return PaymentPrepareResponse.of(payment, transaction, getOrderName(booking));
    }

    private PaymentPrepareResponse prepareServiceRequestPayment(
            User user,
            PaymentPrepareRequest request,
            LocalDateTime now
    ) {
        validateCouponNotUsedForServiceRequest(request.getUserCouponId());

        ServiceRequest serviceRequest = findOwnedServiceRequest(user, request.getTargetId());
        validateServiceRequestPayable(serviceRequest);

        Payment existingReadyPayment = findExistingReadyPayment(serviceRequest);
        if (existingReadyPayment != null && samePrepareCondition(existingReadyPayment, request)) {
            return PaymentPrepareResponse.of(
                    existingReadyPayment,
                    existingReadyPayment.getTransaction(),
                    getOrderName(serviceRequest)
            );
        }

        BigDecimal totalAmount = getServiceRequestAmount(serviceRequest);
        UserCoupon userCoupon = findUsableCoupon(request.getUserCouponId(), user, now);
        BigDecimal discountAmount = calculateDiscountAmount(userCoupon, totalAmount);
        BigDecimal finalAmount = totalAmount.subtract(discountAmount);
        String orderId = createOrderId("REQUEST", serviceRequest.getRequestId(), now);
        User seller = serviceRequest.getExpertService().getExpertProfile().getUser();

        Transaction transaction = prepareServiceRequestTransaction(
                serviceRequest,
                user,
                seller,
                orderId,
                totalAmount,
                discountAmount,
                finalAmount
        );

        Payment payment = prepareReadyPayment(
                transaction,
                user,
                userCoupon,
                request.getPaymentMethod(),
                finalAmount,
                request.getPgProvider()
        );

        return PaymentPrepareResponse.of(payment, transaction, getOrderName(serviceRequest));
    }

    @Transactional
    public PaymentResponse confirmPayment(PaymentConfirmRequest request) {
        Payment payment = paymentRepository.findByTransactionOrderId(request.getOrderId())
                .orElseThrow(() -> GeneralException.of(ErrorCode.PAYMENT_NOT_FOUND));

        Transaction transaction = payment.getTransaction();

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

        validateServiceRequestPaymentHasNoCoupon(payment, transaction);

        LocalDateTime now = LocalDateTime.now();

        if (transaction.getBooking() != null) {
            validateBookingPayable(transaction.getBooking(), now);
        }

        if (!sameAmount(request.getAmount(), transaction.getFinalAmount())) {
            payment.fail("결제 승인 요청 금액과 서버 주문 금액이 일치하지 않습니다.");
            transaction.markFailed();
            throw GeneralException.of(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        validatePgPaymentKeyNotUsed(request.getPaymentKey(), payment);

        LocalDateTime paidAt = now;
        payment.complete(request.getPaymentKey(), paidAt);
        transaction.markPaid();

        if (transaction.getBooking() != null) {
            transaction.getBooking().confirmPayment();
        }

        if (transaction.getServiceRequest() != null && transaction.getServiceRequest().isChatting()) {
            transaction.getServiceRequest().complete();
        }

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

        if (payment.isReady()) {
            payment.cancel(reason);
            transaction.markCancelled();
            if (transaction.getBooking() != null) {
                transaction.getBooking().cancel();
            }
            return PaymentResponse.from(payment);
        }

        if (payment.isPaid()) {
            payment.refund(reason);
            transaction.markRefunded();
            if (transaction.getBooking() != null) {
                transaction.getBooking().cancel();
            }

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

    private Transaction prepareBookingTransaction(
            Booking booking,
            User buyer,
            User seller,
            String orderId,
            BigDecimal totalAmount,
            BigDecimal discountAmount,
            BigDecimal finalAmount
    ) {
        return transactionRepository.findByBookingBookingId(booking.getBookingId())
                .map(transaction -> {
                    if (transaction.isPaid()) {
                        throw GeneralException.of(ErrorCode.PAYMENT_INVALID_STATUS);
                    }

                    transaction.resetForBooking(
                            booking,
                            buyer,
                            seller,
                            orderId,
                            totalAmount,
                            discountAmount,
                            finalAmount
                    );
                    return transaction;
                })
                .orElseGet(() -> transactionRepository.save(Transaction.createForBooking(
                        booking,
                        buyer,
                        seller,
                        orderId,
                        totalAmount,
                        discountAmount,
                        finalAmount
                )));
    }

    private Transaction prepareServiceRequestTransaction(
            ServiceRequest serviceRequest,
            User buyer,
            User seller,
            String orderId,
            BigDecimal totalAmount,
            BigDecimal discountAmount,
            BigDecimal finalAmount
    ) {
        return transactionRepository.findByServiceRequestRequestId(serviceRequest.getRequestId())
                .map(transaction -> {
                    if (transaction.isPaid()) {
                        throw GeneralException.of(ErrorCode.PAYMENT_INVALID_STATUS);
                    }

                    transaction.resetForServiceRequest(
                            serviceRequest,
                            buyer,
                            seller,
                            orderId,
                            totalAmount,
                            discountAmount,
                            finalAmount
                    );
                    return transaction;
                })
                .orElseGet(() -> transactionRepository.save(Transaction.createForServiceRequest(
                        serviceRequest,
                        buyer,
                        seller,
                        orderId,
                        totalAmount,
                        discountAmount,
                        finalAmount
                )));
    }

    private Payment prepareReadyPayment(
            Transaction transaction,
            User user,
            UserCoupon userCoupon,
            String paymentMethod,
            BigDecimal finalAmount,
            String pgProvider
    ) {
        return paymentRepository.findByTransactionTransactionId(transaction.getTransactionId())
                .map(payment -> {
                    if (payment.isPaid()) {
                        throw GeneralException.of(ErrorCode.PAYMENT_INVALID_STATUS);
                    }

                    payment.resetReady(transaction, userCoupon, paymentMethod, finalAmount, pgProvider);
                    return payment;
                })
                .orElseGet(() -> paymentRepository.save(Payment.createReady(
                        transaction,
                        user,
                        userCoupon,
                        paymentMethod,
                        finalAmount,
                        pgProvider
                )));
    }

    private User findLoginUser(User loginUser) {
        if (loginUser == null || loginUser.getUserId() == null) {
            throw GeneralException.of(ErrorCode.UNAUTHORIZED);
        }

        return userRepository.findById(loginUser.getUserId())
                .orElseThrow(() -> GeneralException.of(ErrorCode.UNAUTHORIZED));
    }

    private ServiceRequest findOwnedServiceRequest(
            User user,
            Long requestId
    ) {
        ServiceRequest serviceRequest = serviceRequestRepository
                .findByRequestIdAndDeletedAtIsNull(requestId)
                .orElseThrow(() -> GeneralException.of(ErrorCode.NOT_FOUND));

        if (!serviceRequest.getUser().getUserId().equals(user.getUserId())) {
            throw GeneralException.of(ErrorCode.FORBIDDEN);
        }

        return serviceRequest;
    }

    private void validateServiceRequestPayable(ServiceRequest serviceRequest) {
        if (!serviceRequest.isChatting()) {
            throw GeneralException.of(ErrorCode.BOOKING_NOT_PAYABLE);
        }

        if (serviceRequest.getExpertService() == null
                || serviceRequest.getExpertService().getExpertProfile() == null
                || serviceRequest.getExpertService().getExpertProfile().getUser() == null
                || serviceRequest.getPreferredDate() == null
                || serviceRequest.getBudget() == null
                || serviceRequest.getBudget().compareTo(ZERO) <= 0) {
            throw GeneralException.of(ErrorCode.BOOKING_NOT_PAYABLE);
        }
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
        if (booking.getStoreProduct() == null || booking.getStoreProduct().getPrice() == null) {
            throw GeneralException.of(ErrorCode.BOOKING_NOT_PAYABLE);
        }

        return booking.getStoreProduct().getPrice();
    }

    private BigDecimal getServiceRequestAmount(ServiceRequest serviceRequest) {
        if (serviceRequest.getBudget() == null || serviceRequest.getBudget().compareTo(ZERO) <= 0) {
            throw GeneralException.of(ErrorCode.BOOKING_NOT_PAYABLE);
        }

        return serviceRequest.getBudget();
    }

    private Payment findExistingReadyPayment(Booking booking) {
        return transactionRepository
                .findFirstByBookingBookingIdAndStatusOrderByCreatedAtDesc(
                        booking.getBookingId(),
                        Transaction.STATUS_READY
                )
                .flatMap(transaction -> paymentRepository.findByTransactionTransactionId(transaction.getTransactionId()))
                .filter(Payment::isReady)
                .orElse(null);
    }

    private Payment findExistingReadyPayment(ServiceRequest serviceRequest) {
        return transactionRepository
                .findFirstByServiceRequestRequestIdAndStatusOrderByCreatedAtDesc(
                        serviceRequest.getRequestId(),
                        Transaction.STATUS_READY
                )
                .flatMap(transaction -> paymentRepository.findByTransactionTransactionId(transaction.getTransactionId()))
                .filter(Payment::isReady)
                .orElse(null);
    }

    private User resolveBookingSeller(Booking booking) {
        if (booking.getStoreProduct() != null
                && booking.getStoreProduct().getExpertProfile() != null) {
            return booking.getStoreProduct().getExpertProfile().getUser();
        }

        return null;
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

    private void validateCouponNotUsedForServiceRequest(Long userCouponId) {
        if (userCouponId != null) {
            throw GeneralException.of(ErrorCode.PAYMENT_COUPON_NOT_ALLOWED);
        }
    }

    private void validateServiceRequestPaymentHasNoCoupon(
            Payment payment,
            Transaction transaction
    ) {
        if (transaction.getServiceRequest() == null || payment.getUserCoupon() == null) {
            return;
        }

        throw GeneralException.of(ErrorCode.PAYMENT_COUPON_NOT_ALLOWED);
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
            String targetPrefix,
            Long targetId,
            LocalDateTime now
    ) {
        String shortUuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return targetPrefix + "-" + targetId + "-" + now.format(ORDER_TIME_FORMATTER) + "-" + shortUuid;
    }

    private String getOrderName(Booking booking) {
        if (booking.getStoreProduct() != null
                && booking.getStoreProduct().getTitle() != null
                && !booking.getStoreProduct().getTitle().isBlank()) {
            return booking.getStoreProduct().getTitle();
        }

        return "예약 결제";
    }

    private String getOrderName(ServiceRequest serviceRequest) {
        if (serviceRequest.getTitle() == null || serviceRequest.getTitle().isBlank()) {
            return "견적 요청 결제";
        }

        return serviceRequest.getTitle();
    }
}
