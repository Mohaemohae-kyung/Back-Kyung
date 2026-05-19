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
    private static final String TARGET_TYPE_SERVICE_REQUEST = "SERVICE_REQUEST";
    private static final String TARGET_TYPE_REQUEST = "REQUEST";
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

        /*
         * 결제 화면 최초 진입 응답에서는 쿠폰이 적용되지 않은 기본 금액을 내려줍니다.
         * 실제 쿠폰 적용 금액은 payments/prepare 요청에 userCouponId가 들어왔을 때 다시 계산합니다.
         */
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

        /*
         * 견적 요청 결제 화면은 사용자가 요청한 budget을 기준으로 보여줍니다.
         * 결제 준비 API에서도 같은 requestId를 다시 조회하므로, 프론트가 금액을 조작해 보낼 수 없습니다.
         */
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

        if (TARGET_TYPE_SERVICE_REQUEST.equals(request.getTargetType())
                || TARGET_TYPE_REQUEST.equals(request.getTargetType())) {
            return prepareServiceRequestPayment(user, request, now);
        }

        throw GeneralException.of(ErrorCode.PAYMENT_UNSUPPORTED_TARGET);
    }

    private PaymentPrepareResponse prepareBookingPayment(
            User user,
            PaymentPrepareRequest request,
            LocalDateTime now
    ) {
        /*
         * 마켓 결제 흐름입니다.
         * 사용자가 StoreProduct에서 날짜/시간을 선택해 Booking을 만든 뒤, 그 bookingId로 결제 준비를 요청합니다.
         */
        bookingService.expireStalePendingBookings(now);

        Booking booking = bookingService.findOwnedBooking(user, request.getTargetId());
        validateBookingPayable(booking, now);

        Payment existingReadyPayment = findExistingReadyPayment(booking);
        if (existingReadyPayment != null) {
            if (samePrepareCondition(existingReadyPayment, request)) {
                return PaymentPrepareResponse.of(
                        existingReadyPayment,
                        existingReadyPayment.getTransaction(),
                        getOrderName(booking)
                );
            }

            existingReadyPayment.cancel("결제 조건 변경으로 기존 결제 준비 건을 취소합니다.");
            existingReadyPayment.getTransaction().markCancelled();
        }

        BigDecimal totalAmount = getBookingBaseAmount(booking);
        UserCoupon userCoupon = findUsableCoupon(request.getUserCouponId(), user, now);
        BigDecimal discountAmount = calculateDiscountAmount(userCoupon, totalAmount);
        BigDecimal finalAmount = totalAmount.subtract(discountAmount);

        String orderId = createOrderId("BOOKING", booking.getBookingId(), now);
        User seller = resolveBookingSeller(booking);

        Transaction savedTransaction = transactionRepository.save(Transaction.createForBooking(
                booking,
                user,
                seller,
                orderId,
                totalAmount,
                discountAmount,
                finalAmount
        ));

        Payment savedPayment = paymentRepository.save(Payment.createReady(
                savedTransaction,
                user,
                userCoupon,
                request.getPaymentMethod(),
                finalAmount,
                request.getPgProvider()
        ));

        return PaymentPrepareResponse.of(savedPayment, savedTransaction, getOrderName(booking));
    }

    private PaymentPrepareResponse prepareServiceRequestPayment(
            User user,
            PaymentPrepareRequest request,
            LocalDateTime now
    ) {
        /*
         * 견적 요청 결제 흐름입니다.
         * ServiceRequest는 예약을 만들지 않고, 요청의 budget을 결제 금액으로 사용해 바로 Transaction/Payment를 생성합니다.
         */
        ServiceRequest serviceRequest = findOwnedServiceRequest(user, request.getTargetId());
        validateServiceRequestPayable(serviceRequest);

        Payment existingReadyPayment = findExistingReadyPayment(serviceRequest);
        if (existingReadyPayment != null) {
            if (samePrepareCondition(existingReadyPayment, request)) {
                return PaymentPrepareResponse.of(
                        existingReadyPayment,
                        existingReadyPayment.getTransaction(),
                        getOrderName(serviceRequest)
                );
            }

            existingReadyPayment.cancel("결제 조건 변경으로 기존 결제 준비 건을 취소합니다.");
            existingReadyPayment.getTransaction().markCancelled();
        }

        BigDecimal totalAmount = getServiceRequestAmount(serviceRequest);
        UserCoupon userCoupon = findUsableCoupon(request.getUserCouponId(), user, now);
        BigDecimal discountAmount = calculateDiscountAmount(userCoupon, totalAmount);
        BigDecimal finalAmount = totalAmount.subtract(discountAmount);
        String orderId = createOrderId("REQUEST", serviceRequest.getRequestId(), now);
        User seller = serviceRequest.getExpertService().getExpertProfile().getUser();

        Transaction savedTransaction = transactionRepository.save(Transaction.createForServiceRequest(
                serviceRequest,
                user,
                seller,
                orderId,
                totalAmount,
                discountAmount,
                finalAmount
        ));

        Payment savedPayment = paymentRepository.save(Payment.createReady(
                savedTransaction,
                user,
                userCoupon,
                request.getPaymentMethod(),
                finalAmount,
                request.getPgProvider()
        ));

        return PaymentPrepareResponse.of(savedPayment, savedTransaction, getOrderName(serviceRequest));
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

        /*
         * READY 상태는 아직 PG 승인 전이므로 서버 상태만 취소하면 됩니다.
         * PAID 상태는 실제 운영에서 PG 결제 취소/환불 API 호출이 먼저 성공해야 합니다.
         */
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
        /*
         * main의 새 흐름에서는 고수가 요청을 승인하면 ServiceRequest가 CHATTING 상태가 됩니다.
         * 결제는 고수가 승인한 뒤 사용자와 고수가 협의 중인 상태에서만 열어둡니다.
         */
        if (!serviceRequest.isChatting()) {
            throw GeneralException.of(ErrorCode.BOOKING_NOT_PAYABLE);
        }

        if (serviceRequest.getExpertService() == null
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
        if (booking.getStoreProduct() != null) {
            return booking.getStoreProduct().getPrice();
        }

        ExpertService expertService = booking.getExpertService();

        if (expertService == null || expertService.getBasePrice() == null) {
            throw GeneralException.of(ErrorCode.BOOKING_NOT_PAYABLE);
        }

        return expertService.getBasePrice();
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
                .flatMap(transaction -> paymentRepository.findByTransactionOrderId(transaction.getOrderId()))
                .filter(Payment::isReady)
                .orElse(null);
    }

    private Payment findExistingReadyPayment(ServiceRequest serviceRequest) {
        return transactionRepository
                .findFirstByServiceRequestRequestIdAndStatusOrderByCreatedAtDesc(
                        serviceRequest.getRequestId(),
                        Transaction.STATUS_READY
                )
                .flatMap(transaction -> paymentRepository.findByTransactionOrderId(transaction.getOrderId()))
                .filter(Payment::isReady)
                .orElse(null);
    }

    private User resolveBookingSeller(Booking booking) {
        if (booking.getStoreProduct() != null) {
            return booking.getStoreProduct().getSeller();
        }

        if (booking.getExpertProfile() != null) {
            return booking.getExpertProfile().getUser();
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

        ExpertService expertService = booking.getExpertService();

        if (expertService == null || expertService.getTitle() == null || expertService.getTitle().isBlank()) {
            return "예약 결제";
        }

        return expertService.getTitle();
    }

    private String getOrderName(ServiceRequest serviceRequest) {
        if (serviceRequest.getTitle() == null || serviceRequest.getTitle().isBlank()) {
            return "견적 요청 결제";
        }

        return serviceRequest.getTitle();
    }
}
