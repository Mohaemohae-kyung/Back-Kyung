package kyung.kung_backend.domain.booking.service;

import kyung.kung_backend.domain.booking.dto.BookingPrepareRequest;
import kyung.kung_backend.domain.booking.dto.BookingResponse;
import kyung.kung_backend.domain.booking.entity.Booking;
import kyung.kung_backend.domain.booking.repository.BookingRepository;
import kyung.kung_backend.domain.expert.entity.ExpertProfile;
import kyung.kung_backend.domain.expert.repository.ExpertProfileRepository;
import kyung.kung_backend.domain.servicepost.entity.ExpertService;
import kyung.kung_backend.domain.servicepost.repository.ExpertServiceRepository;
import kyung.kung_backend.domain.store.entity.StoreProduct;
import kyung.kung_backend.domain.store.repository.StoreProductRepository;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.domain.user.repository.UserRepository;
import kyung.kung_backend.global.exception.GeneralException;
import kyung.kung_backend.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingService {

    /*
     * 사용자가 "예약하기"를 누른 뒤 결제 화면에서 머무를 수 있는 시간입니다.
     * 이 시간이 지나면 PENDING_PAYMENT 예약은 EXPIRED로 바뀌고 같은 시간대를 다른 사용자가 예약할 수 있습니다.
     */
    private static final long PAYMENT_HOLD_MINUTES = 15L;

    private static final List<String> BLOCKING_STATUSES = List.of(
            Booking.STATUS_PENDING_PAYMENT,
            Booking.STATUS_CONFIRMED
    );

    private final BookingRepository bookingRepository;
    private final ExpertServiceRepository expertServiceRepository;
    private final StoreProductRepository storeProductRepository;
    private final ExpertProfileRepository expertProfileRepository;
    private final UserRepository userRepository;

    @Transactional
    public BookingResponse prepareBooking(
            User loginUser,
            BookingPrepareRequest request
    ) {
        User user = findLoginUser(loginUser);
        LocalDateTime now = LocalDateTime.now();

        /*
         * 새 슬롯 충돌 검사를 하기 전에 이미 결제 시간이 지난 임시 예약을 정리합니다.
         * 별도 스케줄러가 없어도 사용자가 예약/결제를 시도하는 시점마다 만료 예약이 풀립니다.
         */
        expireStalePendingBookings(now);
        validateBookingTime(request.getStartAt(), request.getEndAt(), now);

        Booking booking = request.getStoreProductId() != null
                ? createStoreProductBooking(user, request, now)
                : createExpertServiceBooking(user, request, now);

        return BookingResponse.from(bookingRepository.save(booking));
    }

    public List<BookingResponse> getMyBookings(User loginUser) {
        User user = findLoginUser(loginUser);

        return bookingRepository.findAllByUserUserIdOrderByCreatedAtDesc(user.getUserId())
                .stream()
                .map(BookingResponse::from)
                .toList();
    }

    public BookingResponse getBookingDetail(
            User loginUser,
            Long bookingId
    ) {
        User user = findLoginUser(loginUser);
        Booking booking = findBooking(bookingId);

        validateOwner(user, booking);

        return BookingResponse.from(booking);
    }

    /*
     * 결제 API에서도 같은 예약 소유권 검증이 필요하므로 public 메서드로 제공합니다.
     * 다른 서비스가 JPA Entity를 안전하게 재사용할 수 있도록 로그인 사용자 재조회까지 수행합니다.
     */
    public Booking findOwnedBooking(
            User loginUser,
            Long bookingId
    ) {
        User user = findLoginUser(loginUser);
        Booking booking = findBooking(bookingId);

        validateOwner(user, booking);

        return booking;
    }

    @Transactional
    public void expireStalePendingBookings(LocalDateTime now) {
        List<Booking> expiredBookings = bookingRepository
                .findAllByStatusAndPaymentExpiresAtBefore(Booking.STATUS_PENDING_PAYMENT, now);

        expiredBookings.forEach(Booking::expirePayment);
    }

    private User findLoginUser(User loginUser) {
        if (loginUser == null || loginUser.getUserId() == null) {
            throw GeneralException.of(ErrorCode.UNAUTHORIZED);
        }

        return userRepository.findById(loginUser.getUserId())
                .orElseThrow(() -> GeneralException.of(ErrorCode.UNAUTHORIZED));
    }

    private ExpertService findExpertService(Long expertServiceId) {
        if (expertServiceId == null) {
            throw GeneralException.of(ErrorCode.BAD_REQUEST);
        }

        return expertServiceRepository.findById(expertServiceId)
                .orElseThrow(() -> GeneralException.of(ErrorCode.NOT_FOUND));
    }

    private StoreProduct findStoreProduct(Long storeProductId) {
        return storeProductRepository.findById(storeProductId)
                .orElseThrow(() -> GeneralException.of(ErrorCode.NOT_FOUND));
    }

    private Booking findBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> GeneralException.of(ErrorCode.BOOKING_NOT_FOUND));
    }

    private void validateOwner(
            User user,
            Booking booking
    ) {
        if (!booking.isOwnedBy(user)) {
            throw GeneralException.of(ErrorCode.FORBIDDEN);
        }
    }

    private void validateBookingTime(
            LocalDateTime startAt,
            LocalDateTime endAt,
            LocalDateTime now
    ) {
        if (startAt == null || endAt == null || !endAt.isAfter(startAt) || startAt.isBefore(now)) {
            throw GeneralException.of(ErrorCode.BOOKING_INVALID_TIME);
        }
    }

    private void validateSlotAvailable(
            Long expertServiceId,
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        /*
         * 시간 겹침 조건:
         * 기존 예약 시작 < 새 예약 종료 && 기존 예약 종료 > 새 예약 시작
         *
         * 예를 들어 14:00~15:00 예약이 있으면 15:00~16:00은 허용되고,
         * 14:30~15:30은 충돌로 막힙니다.
         */
        boolean alreadyReserved = bookingRepository
                .existsByExpertServiceExpertServiceIdAndStartAtLessThanAndEndAtGreaterThanAndStatusIn(
                        expertServiceId,
                        endAt,
                        startAt,
                        BLOCKING_STATUSES
                );

        if (alreadyReserved) {
            throw GeneralException.of(ErrorCode.BOOKING_ALREADY_RESERVED);
        }
    }

    private Booking createStoreProductBooking(
            User user,
            BookingPrepareRequest request,
            LocalDateTime now
    ) {
        StoreProduct storeProduct = findStoreProduct(request.getStoreProductId());
        validateStoreProductReservable(storeProduct);
        validateStoreProductSlotAvailable(storeProduct.getStoreProductId(), request.getStartAt(), request.getEndAt());

        ExpertProfile expertProfile = expertProfileRepository.findByUserUserId(storeProduct.getSeller().getUserId())
                .orElseThrow(() -> GeneralException.of(ErrorCode.NOT_FOUND));

        return Booking.createPendingPaymentForStoreProduct(
                user,
                storeProduct,
                expertProfile,
                request.getStartAt(),
                request.getEndAt(),
                request.getLocationText(),
                now.plusMinutes(PAYMENT_HOLD_MINUTES)
        );
    }

    private Booking createExpertServiceBooking(
            User user,
            BookingPrepareRequest request,
            LocalDateTime now
    ) {
        ExpertService expertService = findExpertService(request.getExpertServiceId());
        validateSlotAvailable(expertService.getExpertServiceId(), request.getStartAt(), request.getEndAt());

        return Booking.createPendingPayment(
                user,
                expertService,
                request.getStartAt(),
                request.getEndAt(),
                request.getLocationText(),
                now.plusMinutes(PAYMENT_HOLD_MINUTES)
        );
    }

    private void validateStoreProductReservable(StoreProduct storeProduct) {
        if (storeProduct.getSeller() == null
                || storeProduct.getPrice() == null
                || storeProduct.getPrice().signum() < 0
                || !"ACTIVE".equals(storeProduct.getStatus())
                || storeProduct.getDeletedAt() != null
                || storeProduct.getStockQuantity() == null
                || storeProduct.getStockQuantity() <= 0) {
            throw GeneralException.of(ErrorCode.BAD_REQUEST);
        }
    }

    private void validateStoreProductSlotAvailable(
            Long storeProductId,
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        boolean alreadyReserved = bookingRepository
                .existsByStoreProductStoreProductIdAndStartAtLessThanAndEndAtGreaterThanAndStatusIn(
                        storeProductId,
                        endAt,
                        startAt,
                        BLOCKING_STATUSES
                );

        if (alreadyReserved) {
            throw GeneralException.of(ErrorCode.BOOKING_ALREADY_RESERVED);
        }
    }
}
