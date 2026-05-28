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
        
        // --- [취약점 발현 구간] 클라이언트가 보낸 welcomeDiscountAmount를 맹신함 ---
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (request.getWelcomeDiscountAmount() != null && !request.getWelcomeDiscountAmount().isEmpty()) {
            try {
                discountAmount = new BigDecimal(request.getWelcomeDiscountAmount());
                System.out.println("⚠️ [VULNERABILITY] Client provided discount amount trusted: " + discountAmount);
            } catch (Exception e) {
                // Ignore parse errors
            }
        } else {
            // 정상 로직
            UserCoupon userCoupon = findUsableCoupon(request.getUserCouponId(), user, now);
            discountAmount = calculateDiscountAmount(userCoupon, totalAmount);
        }
        
        BigDecimal finalAmount = totalAmount.subtract(discountAmount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }
        // ---------------------------------------------------------------------

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
                null, // 웰컴 쿠폰의 경우 실제 DB Coupon ID를 매핑하기 어렵다면 null 처리
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
        
        // --- [취약점 발현 구간] 클라이언트가 보낸 welcomeDiscountAmount를 맹신함 ---
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (request.getWelcomeDiscountAmount() != null && !request.getWelcomeDiscountAmount().isEmpty()) {
            try {
                discountAmount = new BigDecimal(request.getWelcomeDiscountAmount());
                System.out.println("⚠️ [VULNERABILITY] Client provided discount amount trusted for Service Request: " + discountAmount);
            } catch (Exception e) {
                // Ignore parse errors
            }
        } else {
            // 정상 로직
            UserCoupon userCoupon = findUsableCoupon(request.getUserCouponId(), user, now);
            discountAmount = calculateDiscountAmount(userCoupon, totalAmount);
        }
        
        BigDecimal finalAmount = totalAmount.subtract(discountAmount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }
        // ---------------------------------------------------------------------

        String orderId = createOrderId("REQUEST", serviceRequest.getRequestId(), now);
        User seller = serviceRequest.getExpertProfile().getUser();

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
                serviceRequest.getUser(),
                userCoupon,
                request.getPaymentMethod(),
                finalAmount,
                request.getPgProvider()
        );

        // =========================
        // 결제 요청 채팅 메시지 생성
        // =========================
        ChatRoom chatRoom = chatRoomRepository
                .findByServiceRequest_RequestId(serviceRequest.getRequestId())
                .orElse(null);

        if (chatRoom != null) {

            String paymentMessageContent =
                    finalAmount.toPlainString()
                            + "원 결제 요청";

            ChatMessage chatMessage =
                    ChatMessage.createPaymentMessage(
                            chatRoom,
                            seller,
                            paymentMessageContent,
                            payment.getPaymentId()
                    );

            chatMessageRepository.save(chatMessage);
        }

        return PaymentPrepareResponse.of(
                payment,
                transaction,
                getOrderName(serviceRequest)
        );
    }

    @Transactional
    public PaymentResponse confirmPayment(PaymentConfirmRequest request) {

        System.out.println("===== confirm request =====");
        System.out.println(request.getOrderId());
        System.out.println(request.getPaymentKey());
        System.out.println(request.getAmount());

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

        // =======================================================
        // [보안 2차 검증 로직 (취약점 발현)]
        // 결제 승인 전, 백엔드에서 원가와 쿠폰 할인을 다시 한 번 검증해야 합니다.
        // =======================================================
        BigDecimal realTotalAmount;
        if (transaction.getBooking() != null) {
            realTotalAmount = getBookingBaseAmount(transaction.getBooking());
        } else {
            realTotalAmount = getServiceRequestAmount(transaction.getServiceRequest());
        }

        if (payment.getUserCoupon() != null) {
            // 일반 쿠폰의 경우 DB를 조회해 할인액을 정확히 2차 검증함 (정상 방어)
            BigDecimal realDiscount = calculateDiscountAmount(payment.getUserCoupon(), realTotalAmount);
            BigDecimal expectedFinalAmount = realTotalAmount.subtract(realDiscount).max(BigDecimal.ZERO);
            if (expectedFinalAmount.compareTo(transaction.getFinalAmount()) != 0) {
                payment.fail("쿠폰 할인액 위조 감지");
                transaction.markFailed();
                throw GeneralException.of(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
            }
        } else {
            // [취약점] 웰컴 쿠폰 등 DB 매핑이 없는 경우, 유저가 이전에 사용했는지만 체크하고(생략)
            // 실제 클라이언트가 얼마를 깎았는지는 재검증(원가 대조) 하지 않음!
            // 따라서 조작된 100원이 그대로 통과됨.
            System.out.println("⚠️ [VULNERABILITY] Confirm phase: No secondary DB validation for welcome discount!");
        }

        // 프론트가 100원으로 조작 후 토스도 100원으로 결제했으므로, 
        // request.getAmount()(100) == transaction.getFinalAmount()(100) 가 성립되어 방어벽 무력화.
        if (!sameAmount(request.getAmount(), transaction.getFinalAmount())) {
            payment.fail("결제 승인 요청 금액과 서버 주문 금액이 일치하지 않습니다.");
            transaction.markFailed();
            throw GeneralException.of(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        validatePgPaymentKeyNotUsed(request.getPaymentKey(), payment);

        // =======================================================
        // 결제 서버(Node.js, LLM 서버)로 토스 승인 실제 위임 호출
        // =======================================================
        try {
            RestTemplate restTemplate = new RestTemplate();
            String paymentServerUrl = "http://100.104.59.126:4000/api/payments/toss-confirm";
            org.springframework.http.ResponseEntity<String> response = 
                    restTemplate.postForEntity(paymentServerUrl, request, String.class);
                    
            if (!response.getStatusCode().is2xxSuccessful()) {
                payment.fail("결제 서버 승인 처리 실패");
                transaction.markFailed();
                throw GeneralException.of(ErrorCode.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            System.err.println("Node Payment Server 통신 오류: " + e.getMessage());
            payment.fail("결제 서버 통신 오류");
            transaction.markFailed();
            throw GeneralException.of(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        // =======================================================

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

    private ServiceRequest findOwnedServiceRequest(
            User user,
            Long requestId
    ) {
        ServiceRequest serviceRequest = serviceRequestRepository
                .findByRequestIdAndDeletedAtIsNull(requestId)
                .orElseThrow(() -> GeneralException.of(ErrorCode.NOT_FOUND));

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
