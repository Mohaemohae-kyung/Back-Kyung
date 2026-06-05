package kyung.kung_backend.domain.payment.service;

import kyung.kung_backend.domain.booking.entity.Booking;
import kyung.kung_backend.domain.booking.service.BookingService;
import kyung.kung_backend.domain.chat.entity.ChatMessage;
import kyung.kung_backend.domain.chat.entity.ChatRoom;
import kyung.kung_backend.domain.chat.repository.ChatMessageRepository;
import kyung.kung_backend.domain.chat.repository.ChatRoomRepository;
import kyung.kung_backend.domain.coupon.entity.Coupon;
import kyung.kung_backend.domain.coupon.entity.UserCoupon;
import kyung.kung_backend.domain.coupon.repository.UserCouponRepository;
import kyung.kung_backend.domain.payment.dto.*;
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
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
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
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final kyung.kung_backend.global.fcm.FcmService fcmService;

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
    public PaymentResponse createServiceRequestPaymentRequest(
            User loginUser,
            Long requestId,
            ServiceRequestPaymentRequestCreateRequest request
    ) {
        User expert = findLoginUser(loginUser);
        ServiceRequest serviceRequest = findServiceRequest(requestId);
        validateServiceRequestExpert(expert, serviceRequest);
        validateServiceRequestPayable(serviceRequest);

        User buyer = serviceRequest.getUser();
        User seller = serviceRequest.getExpertProfile().getUser();
        BigDecimal amount = getServiceRequestAmount(serviceRequest);
        String orderId = createOrderId("REQUEST", requestId, LocalDateTime.now());

        Transaction transaction = prepareServiceRequestTransaction(
                serviceRequest,
                buyer,
                seller,
                orderId,
                amount,
                ZERO,
                amount
        );

        Payment payment = prepareReadyPayment(
                transaction,
                buyer,
                null,
                request.getPaymentMethod(),
                amount,
                request.getPgProvider()
        );

        createServiceRequestPaymentMessage(serviceRequest, seller, amount, payment.getPaymentId());

        return PaymentResponse.from(payment);
    }

    // =========================================================================
    // Node.js 결제 전담 서버를 위한 내부 API (백엔드는 E2E 데이터를 모르고 통신)
    // =========================================================================
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getTargetInfoForNode(String targetType, Long targetId, Long userId, Long userCouponId) {
        User user = userRepository.findById(userId).orElseThrow(() -> GeneralException.of(ErrorCode.UNAUTHORIZED));
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        BigDecimal baseAmount = BigDecimal.ZERO;
        
        if (TARGET_TYPE_BOOKING.equals(targetType)) {
            Booking booking = bookingService.findOwnedBooking(user, targetId);
            baseAmount = getBookingBaseAmount(booking);
            result.put("baseAmount", baseAmount);
            result.put("orderName", getOrderName(booking));
        } else if (TARGET_TYPE_SERVICE_REQUEST.equals(targetType)) {
            ServiceRequest serviceRequest = findRequesterServiceRequest(user, targetId);
            baseAmount = getServiceRequestAmount(serviceRequest);
            result.put("baseAmount", baseAmount);
            result.put("orderName", getOrderName(serviceRequest));
        } else {
            throw GeneralException.of(ErrorCode.PAYMENT_UNSUPPORTED_TARGET);
        }
        
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (userCouponId != null) {
            UserCoupon userCoupon = findUsableCoupon(userCouponId, user, LocalDateTime.now());
            discountAmount = calculateDiscountAmount(userCoupon, baseAmount);
        }
        result.put("discountAmount", discountAmount);
        
        return result;
    }

    @Transactional
    public Long prepareReadyPaymentProxy(java.util.Map<String, Object> request) {
        String targetType = (String) request.get("targetType");
        Long targetId = Long.valueOf(request.get("targetId").toString());
        Long userId = Long.valueOf(request.get("userId").toString());
        String orderId = (String) request.get("orderId");
        BigDecimal finalAmount = new BigDecimal(request.get("finalAmount").toString());
        String paymentMethod = (String) request.get("paymentMethod");
        String pgProvider = (String) request.get("pgProvider");
        
        Long userCouponId = null;
        if (request.get("userCouponId") != null) {
            userCouponId = Long.valueOf(request.get("userCouponId").toString());
        }

        User user = userRepository.findById(userId).orElseThrow(() -> GeneralException.of(ErrorCode.UNAUTHORIZED));
        LocalDateTime now = LocalDateTime.now();

        Transaction transaction;
        User seller;
        UserCoupon userCoupon = userCouponId != null ? findUsableCoupon(userCouponId, user, now) : null;
        Payment payment = null;

        if (TARGET_TYPE_BOOKING.equals(targetType)) {
            Booking booking = bookingService.findOwnedBooking(user, targetId);
            seller = resolveBookingSeller(booking);
            transaction = prepareBookingTransaction(booking, user, seller, orderId, finalAmount, BigDecimal.ZERO, finalAmount);
            payment = prepareReadyPayment(transaction, user, userCoupon, paymentMethod, finalAmount, pgProvider);
        } else if (TARGET_TYPE_SERVICE_REQUEST.equals(targetType)) {
            ServiceRequest serviceRequest = findRequesterServiceRequest(user, targetId);
            seller = serviceRequest.getExpertProfile().getUser();
            transaction = prepareServiceRequestTransaction(serviceRequest, user, seller, orderId, finalAmount, BigDecimal.ZERO, finalAmount);
            payment = prepareReadyPayment(transaction, user, userCoupon, paymentMethod, finalAmount, pgProvider);
        } else {
            throw GeneralException.of(ErrorCode.PAYMENT_UNSUPPORTED_TARGET);
        }

        return payment.getPaymentId();
    }

    @Transactional
    public void completePaymentFromNode(java.util.Map<String, Object> request) {
        String targetType = (String) request.get("targetType");
        Long targetId = Long.valueOf(request.get("targetId").toString());
        Long userId = Long.valueOf(request.get("userId").toString());
        String orderId = (String) request.get("orderId");
        BigDecimal finalAmount = new BigDecimal(request.get("finalAmount").toString());
        String paymentKey = (String) request.get("paymentKey");
        String paymentMethod = (String) request.get("paymentMethod");
        String pgProvider = (String) request.get("pgProvider");
        
        Long userCouponId = null;
        if (request.get("userCouponId") != null) {
            userCouponId = Long.valueOf(request.get("userCouponId").toString());
        }

        User user = userRepository.findById(userId).orElseThrow(() -> GeneralException.of(ErrorCode.UNAUTHORIZED));
        LocalDateTime now = LocalDateTime.now();

        Transaction transaction;
        User seller;
        UserCoupon userCoupon = userCouponId != null ? findUsableCoupon(userCouponId, user, now) : null;

        if (TARGET_TYPE_BOOKING.equals(targetType)) {
            Booking booking = bookingService.findOwnedBooking(user, targetId);
            seller = resolveBookingSeller(booking);
            transaction = prepareBookingTransaction(booking, user, seller, orderId, finalAmount, BigDecimal.ZERO, finalAmount);
            booking.confirmPayment();
        } else if (TARGET_TYPE_SERVICE_REQUEST.equals(targetType)) {
            ServiceRequest serviceRequest = findRequesterServiceRequest(user, targetId);
            seller = serviceRequest.getExpertProfile().getUser();
            transaction = prepareServiceRequestTransaction(serviceRequest, user, seller, orderId, finalAmount, BigDecimal.ZERO, finalAmount);
            serviceRequest.complete();

            // =========================
            // 결제 요청 완료 채팅 메시지 생성
            // =========================
            ChatRoom chatRoom = chatRoomRepository.findByServiceRequest_RequestId(serviceRequest.getRequestId()).orElse(null);
            if (chatRoom != null) {
                String paymentMessageContent = finalAmount.toPlainString() + "원 결제 완료";
                ChatMessage chatMessage = ChatMessage.createPaymentMessage(chatRoom, seller, paymentMessageContent, null);
                chatMessageRepository.save(chatMessage);
            }

            // =========================
            // FCM 푸시 알림 (고수에게)
            // =========================
            String buyerName = user.getNickname() != null ? user.getNickname() : user.getName();
            String title = "결제 완료";
            String body = buyerName + "님으로부터 " + finalAmount.toPlainString() + "원 결제 완료 (주문 " + orderId + ")";
            fcmService.sendPaymentNotification(seller.getFcmToken(), title, body, orderId);
        } else {
            throw GeneralException.of(ErrorCode.PAYMENT_UNSUPPORTED_TARGET);
        }

        Payment payment = prepareReadyPayment(transaction, user, userCoupon, paymentMethod, finalAmount, pgProvider);
        payment.complete(paymentKey, now);
        transaction.markPaid();

        if (userCoupon != null) {
            userCoupon.use(now);
        }
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
        findLoginUser(loginUser);

        Payment payment = paymentRepository.findById(paymentId)
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

                    payment.resetReady(
                            transaction,
                            user,
                            userCoupon,
                            paymentMethod,
                            finalAmount,
                            pgProvider
                    );
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

    @Transactional
    public void updatePaymentPasswordHash(java.util.Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        String hash = (String) request.get("hash");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> GeneralException.of(ErrorCode.NOT_FOUND));
        user.updatePaymentPasswordHash(hash);
    }

    public java.util.Map<String, Object> getPaymentPasswordHashAndStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> GeneralException.of(ErrorCode.NOT_FOUND));
        
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("hash", user.getPaymentPasswordHash());
        result.put("status", user.getStatus() != null ? user.getStatus() : "ACTIVE");
        return result;
    }

    @Transactional
    public void suspendUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> GeneralException.of(ErrorCode.NOT_FOUND));
        user.suspend();
    }

    private ServiceRequest findServiceRequest(Long requestId) {
        return serviceRequestRepository
                .findByRequestIdAndDeletedAtIsNull(requestId)
                .orElseThrow(() -> GeneralException.of(ErrorCode.NOT_FOUND));
    }

    private ServiceRequest findRequesterServiceRequest(
            User user,
            Long requestId
    ) {
        ServiceRequest serviceRequest = findServiceRequest(requestId);

        if (!serviceRequest.getUser().getUserId().equals(user.getUserId())) {
            throw GeneralException.of(ErrorCode.FORBIDDEN);
        }

        return serviceRequest;
    }

    private void validateServiceRequestExpert(
            User user,
            ServiceRequest serviceRequest
    ) {
        if (serviceRequest.getExpertProfile() == null
                || serviceRequest.getExpertProfile().getUser() == null
                || !serviceRequest.getExpertProfile().getUser().getUserId().equals(user.getUserId())) {
            throw GeneralException.of(ErrorCode.FORBIDDEN);
        }
    }

    private void createServiceRequestPaymentMessage(
            ServiceRequest serviceRequest,
            User seller,
            BigDecimal finalAmount,
            Long paymentId
    ) {
        ChatRoom chatRoom = chatRoomRepository
                .findByServiceRequest_RequestId(serviceRequest.getRequestId())
                .orElseThrow(() -> GeneralException.of(ErrorCode.NOT_FOUND));

        String paymentMessageContent = finalAmount.toPlainString() + "원 결제 요청";
        ChatMessage chatMessage = ChatMessage.createPaymentMessage(
                chatRoom,
                seller,
                paymentMessageContent,
                paymentId
        );
        chatMessageRepository.save(chatMessage);
    }

    private ServiceRequest findOwnedServiceRequest(
            User user,
            Long requestId
    ) {
        ServiceRequest serviceRequest = findServiceRequest(requestId);

        Long requesterId =
                serviceRequest.getUser().getUserId();

        Long expertId =
                serviceRequest.getExpertProfile()
                        .getUser()
                        .getUserId();

        // =========================
        // 요청자 또는 고수만 접근 가능
        if (
                !requesterId.equals(user.getUserId())
                        &&
                        !expertId.equals(user.getUserId())
        ){
            throw GeneralException.of(ErrorCode.FORBIDDEN);
        }

        return serviceRequest;
    }

    private void validateServiceRequestPayable(ServiceRequest serviceRequest) {
        if (!serviceRequest.isChatting()) {
            throw GeneralException.of(ErrorCode.BOOKING_NOT_PAYABLE);
        }

        if (serviceRequest.getExpertProfile() == null
                || serviceRequest.getExpertProfile().getUser() == null
                || serviceRequest.getCategory() == null
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

    // PaymentService.java 내부의 쿠폰 계산 로직 수정 예시
    private BigDecimal calculateDiscountAmount(UserCoupon userCoupon, BigDecimal totalAmount) {
        if (userCoupon == null) {
            return BigDecimal.ZERO;
        }

        Coupon coupon = userCoupon.getCoupon();

        // 최소 주문 금액 검증 로직(minOrderAmount) 및 정률 할인(RATE) 분기 로직을 모두 제거합니다.
        // 오직 고정 할인 금액(discountAmount)만 바로 반환하도록 변경합니다.
        BigDecimal discountAmount = coupon.getDiscountAmount();

        // 할인 금액이 총 주문 금액보다 크다면 주문 금액만큼만 할인하도록 방어 코드를 작성합니다.
        if (discountAmount.compareTo(totalAmount) > 0) {
            return totalAmount;
        }

        return discountAmount;
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
