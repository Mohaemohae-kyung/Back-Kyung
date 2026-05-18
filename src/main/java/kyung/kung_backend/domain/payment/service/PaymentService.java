package kyung.kung_backend.domain.payment.service;

import kyung.kung_backend.domain.booking.entity.Booking;
import kyung.kung_backend.domain.booking.repository.BookingRepository;
import kyung.kung_backend.domain.match.entity.Match;
import kyung.kung_backend.domain.match.repository.MatchRepository;
import kyung.kung_backend.domain.payment.dto.PaymentPrepareRequest;
import kyung.kung_backend.domain.payment.dto.PaymentPrepareResponse;
import kyung.kung_backend.domain.payment.entity.Payment;
import kyung.kung_backend.domain.payment.repository.PaymentRepository;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 결제 도메인 비즈니스 로직을 담당하는 Service입니다.
 *
 * 호출 흐름:
 * - 클라이언트 또는 Swagger
 *   -> PaymentController.prepare()
 *   -> PaymentService.prepare()
 *   -> BookingRepository / MatchRepository / TransactionRepository / PaymentRepository
 *
 * 현재 구현 범위:
 * - issue #4 중 "결제 준비 API"의 스켈레톤입니다.
 * - 실제 PG 결제 승인(confirm), 환불, 관리자 환불 승인 로직은 아직 구현하지 않습니다.
 * - main 브랜치의 JWT 인증 구조에 맞춰 Controller에서 전달된 로그인 User를 검증합니다.
 * - 로그인 User와 Booking.user가 다르면 결제 준비를 막습니다.
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final String TRANSACTION_STATUS_READY = "READY";
    private static final String PAYMENT_STATUS_READY = "READY";
    private static final DateTimeFormatter ORDER_ID_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final Set<String> SUPPORTED_PAYMENT_METHODS = Set.of("CARD", "SERVICE_PAY");

    private final PaymentRepository paymentRepository;
    private final TransactionRepository transactionRepository;
    private final BookingRepository bookingRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;

    /**
     * 결제 준비 API의 핵심 로직입니다.
     *
     * 어떤 일을 하는지:
     * 1. 로그인 사용자가 유효한지 확인합니다.
     * 2. 요청 DTO에서 결제수단과 bookingId를 검증합니다.
     * 3. 결제 대상 Booking/Match를 조회합니다.
     * 4. 로그인 사용자가 예약 소유자인지 검증합니다.
     * 5. 이미 결제 준비된 예약인지 확인합니다.
     * 6. Match.proposedPrice를 결제 금액으로 사용합니다.
     * 7. 서버 주문번호(orderId)를 생성합니다.
     * 8. Transaction을 READY 상태로 저장합니다.
     * 9. Payment를 READY 상태로 저장합니다.
     * 10. Swagger/클라이언트가 확인할 수 있도록 PaymentPrepareResponse를 반환합니다.
     *
     * 어디서 불리는지:
     * - PaymentController.prepare()에서 호출됩니다.
     *
     * 이후 확장 지점:
     * - 쿠폰/할인 금액 반영
     * - PG사별 결제 준비용 추가 응답값 생성
     * - 결제 가능 예약 상태 검증
     */
    @Transactional
    public PaymentPrepareResponse prepare(User loginUser, PaymentPrepareRequest request) {
        User user = findLoginUser(loginUser);
        validatePaymentMethod(request.getPaymentMethod());
        PaymentTarget target = resolvePaymentTarget(request);
        validateBookingOwner(user, target.booking());
        validatePaymentNotPrepared(target.booking());
        BigDecimal amount = resolvePaymentAmount(target.match());
        String orderId = generateUniqueOrderId();

        Transaction transaction = Transaction.prepareServiceBookingPayment(
                target.booking(),
                user,
                target.seller(),
                amount,
                TRANSACTION_STATUS_READY
        );
        Transaction savedTransaction = transactionRepository.save(transaction);

        Payment payment = Payment.prepare(
                savedTransaction,
                user,
                orderId,
                request.getPaymentMethod(),
                amount,
                PAYMENT_STATUS_READY
        );
        Payment savedPayment = paymentRepository.save(payment);

        return PaymentPrepareResponse.from(savedPayment);
    }

    /**
     * 요청 DTO를 기준으로 결제 대상을 찾습니다.
     *
     * 현재 정책:
     * - prepare API는 Booking을 기준으로 결제 준비 데이터를 생성합니다.
     * - Transaction 엔티티가 Booking 또는 Purchase에 연결되는 구조라서 bookingId가 필요합니다.
     * - matchId는 필수는 아니지만, 들어오면 Booking이 가진 Match와 같은지 검증합니다.
     *
     * bookingId와 matchId가 둘 다 있는 경우:
     * - booking.match.matchId와 요청 matchId가 같은지 검증합니다.
     * - 서로 다르면 사용자가 다른 매칭/예약 정보를 섞어 보낸 것이므로 예외를 던집니다.
     *
     * matchId만으로 결제 준비를 하고 싶다면:
     * - 결제 전 Booking을 자동 생성하는 정책을 추가하거나,
     * - Transaction에 Match 연결 필드를 추가해야 합니다.
     * - 현재 스켈레톤에서는 DB 모델과 맞추기 위해 bookingId를 요구합니다.
     */
    private PaymentTarget resolvePaymentTarget(PaymentPrepareRequest request) {
        if (!request.hasBookingId()) {
            throw GeneralException.of(ErrorCode.PAYMENT_PREPARE_TARGET_REQUIRED);
        }

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> GeneralException.of(ErrorCode.PAYMENT_BOOKING_NOT_FOUND));

        Match match = booking.getMatch();

        if (request.hasMatchId()) {
            validateRequestedMatchExists(request.getMatchId());
            validateBookingMatch(request.getMatchId(), match);
        }

        User seller = booking.getExpertProfile().getUser();
        return new PaymentTarget(booking, match, seller);
    }

    /**
     * 로그인 사용자를 DB 기준으로 다시 확인합니다.
     *
     * 어디서 불리는지:
     * - prepare()의 가장 앞에서 호출합니다.
     *
     * 왜 필요한지:
     * - Controller의 @AuthenticationPrincipal User는 JWT 필터가 SecurityContext에 넣어준 사용자입니다.
     * - 인증이 없거나, 토큰의 userId에 해당하는 사용자가 DB에 없으면 결제 준비를 진행하면 안 됩니다.
     * - main 브랜치의 다른 서비스(ServiceRequestService)와 같은 방식으로 UNAUTHORIZED를 반환합니다.
     */
    private User findLoginUser(User loginUser) {
        if (loginUser == null || loginUser.getUserId() == null) {
            throw GeneralException.of(ErrorCode.UNAUTHORIZED);
        }

        return userRepository.findById(loginUser.getUserId())
                .orElseThrow(() -> GeneralException.of(ErrorCode.UNAUTHORIZED));
    }

    /**
     * 로그인 사용자가 예약의 결제 주체인지 확인합니다.
     *
     * 어디서 불리는지:
     * - prepare()에서 Booking 조회 직후 호출합니다.
     *
     * 왜 필요한지:
     * - 사용자는 본인의 예약에 대해서만 결제 준비를 할 수 있어야 합니다.
     * - 다른 사용자의 bookingId를 알고 있더라도 결제 준비를 만들 수 없도록 막습니다.
     */
    private void validateBookingOwner(User user, Booking booking) {
        if (!booking.getUser().getUserId().equals(user.getUserId())) {
            throw GeneralException.of(ErrorCode.FORBIDDEN);
        }
    }

    /**
     * 결제 수단을 검증합니다.
     *
     * 현재 허용 값:
     * - CARD
     * - SERVICE_PAY
     *
     * 어디서 불리는지:
     * - prepare() 시작 지점에서 호출합니다.
     *
     * 왜 먼저 검증하는지:
     * - 결제 수단이 잘못된 요청은 DB 조회를 하기 전에 빠르게 실패시키는 것이 좋습니다.
     * - 나중에 PaymentMethod enum을 만들면 이 메서드는 enum 변환/검증 로직으로 교체하면 됩니다.
     */
    private void validatePaymentMethod(String paymentMethod) {
        if (paymentMethod == null || !SUPPORTED_PAYMENT_METHODS.contains(paymentMethod)) {
            throw GeneralException.of(ErrorCode.PAYMENT_INVALID_METHOD);
        }
    }

    /**
     * 요청으로 들어온 matchId가 실제 존재하는지 확인합니다.
     *
     * Booking이 이미 Match를 가지고 있으므로 prepare 저장 자체에는 추가 조회가 꼭 필요하지 않습니다.
     * 그래도 사용자가 보낸 matchId가 완전히 존재하지 않는 값인지와,
     * 존재하지만 booking의 match와 다른 값인지를 분리해 응답하기 위해 조회합니다.
     */
    private void validateRequestedMatchExists(Long matchId) {
        if (!matchRepository.existsById(matchId)) {
            throw GeneralException.of(ErrorCode.PAYMENT_MATCH_NOT_FOUND);
        }
    }

    /**
     * 요청 matchId와 예약에 연결된 matchId가 같은지 검증합니다.
     *
     * 예:
     * - bookingId=2는 matchId=2에 연결되어 있는데 요청 Body에 matchId=3을 보내면 실패합니다.
     */
    private void validateBookingMatch(Long requestedMatchId, Match bookingMatch) {
        if (!requestedMatchId.equals(bookingMatch.getMatchId())) {
            throw GeneralException.of(ErrorCode.PAYMENT_BOOKING_MATCH_MISMATCH);
        }
    }

    /**
     * 같은 예약에 대해 이미 거래가 생성되어 있는지 확인합니다.
     *
     * 어디서 불리는지:
     * - prepare()에서 결제 금액 계산과 저장을 하기 전에 호출합니다.
     *
     * 왜 필요한지:
     * - Transaction.BOOKING_ID는 unique 제약이 있어 같은 bookingId로 두 번 저장할 수 없습니다.
     * - DB 제약 오류 대신 PAYMENT_409_001 응답을 주기 위해 Service에서 먼저 확인합니다.
     */
    private void validatePaymentNotPrepared(Booking booking) {
        if (transactionRepository.existsByBooking_BookingId(booking.getBookingId())) {
            throw GeneralException.of(ErrorCode.PAYMENT_ALREADY_PREPARED);
        }
    }

    /**
     * 결제 금액을 계산합니다.
     *
     * 현재 정책:
     * - Match.proposedPrice를 결제 금액으로 사용합니다.
     *
     * 이후 확장:
     * - 쿠폰 할인, 숨고페이 포인트, 부가세, 수수료 정책이 생기면 이 메서드에서 계산 흐름을 확장합니다.
     */
    private BigDecimal resolvePaymentAmount(Match match) {
        BigDecimal amount = match.getProposedPrice();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw GeneralException.of(ErrorCode.PAYMENT_INVALID_AMOUNT);
        }

        return amount;
    }

    /**
     * PG 승인 단계에서 사용할 서버 주문번호를 생성합니다.
     *
     * 사용 위치:
     * - prepare 응답으로 클라이언트에 내려갑니다.
     * - 이후 confirm API에서 PG사가 돌려준 orderId와 서버에 저장된 Payment.orderId를 비교합니다.
     *
     * 생성 규칙:
     * - ORDER-현재시각-랜덤문자 형태입니다.
     * - DB에 같은 orderId가 있는지 확인하고, 충돌하면 몇 번 재시도합니다.
     */
    private String generateUniqueOrderId() {
        for (int attempt = 0; attempt < 5; attempt++) {
            String orderId = "ORDER-"
                    + LocalDateTime.now().format(ORDER_ID_TIME_FORMATTER)
                    + "-"
                    + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);

            if (!paymentRepository.existsByOrderId(orderId)) {
                return orderId;
            }
        }

        throw GeneralException.of(ErrorCode.PAYMENT_ORDER_ID_GENERATION_FAILED);
    }

    /**
     * prepare 로직 내부에서만 사용하는 결제 대상 묶음입니다.
     *
     * record를 사용해 Booking, Match, 판매자를 한 번에 전달합니다.
     * 구매자는 로그인 사용자 검증이 끝난 User를 그대로 사용하므로 이 record에 별도로 넣지 않습니다.
     * 외부 API 응답으로 나가는 객체가 아니므로 private으로 제한합니다.
     */
    private record PaymentTarget(
            Booking booking,
            Match match,
            User seller
    ) {
    }
}
