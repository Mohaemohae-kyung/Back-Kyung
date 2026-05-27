package kyung.kung_backend.domain.booking.service;

import kyung.kung_backend.domain.booking.dto.BookingPrepareRequest;
import kyung.kung_backend.domain.booking.dto.BookingResponse;
import kyung.kung_backend.domain.booking.entity.Booking;
import kyung.kung_backend.domain.booking.repository.BookingRepository;
import kyung.kung_backend.domain.location.entity.Location;
import kyung.kung_backend.domain.location.repository.LocationRepository;
import kyung.kung_backend.domain.store.entity.StoreProduct;
import kyung.kung_backend.domain.store.entity.enums.StoreProductServiceType;
import kyung.kung_backend.domain.store.entity.enums.StoreProductStatus;
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
     * 사용자가 예약하기를 누른 뒤 결제 화면에서 머물 수 있는 시간입니다.
     * 이 시간이 지나면 PENDING_PAYMENT 예약은 EXPIRED로 바뀌고 같은 시간을 다시 예약할 수 있습니다.
     */
    private static final long PAYMENT_HOLD_MINUTES = 15L;

    private static final List<String> BLOCKING_STATUSES = List.of(
            Booking.STATUS_PENDING_PAYMENT,
            Booking.STATUS_CONFIRMED
    );

    private final BookingRepository bookingRepository;
    private final StoreProductRepository storeProductRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;

    @Transactional
    public BookingResponse prepareBooking(
            User loginUser,
            BookingPrepareRequest request
    ) {
        User user = findLoginUser(loginUser);
        LocalDateTime now = LocalDateTime.now();

        expireStalePendingBookings(now);
        validateBookingTime(request.getStartAt(), request.getEndAt(), now);

        if (request.getStoreProductId() == null) {
            throw GeneralException.of(ErrorCode.BAD_REQUEST);
        }

        Booking booking = createStoreProductBooking(user, request, now);

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
        
        // 예약 조회 권한검증 누락 취약점
        // validateOwner(user, booking);

        return BookingResponse.from(booking);
    }

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

    private Booking createStoreProductBooking(
            User user,
            BookingPrepareRequest request,
            LocalDateTime now
    ) {
        StoreProduct storeProduct = findStoreProduct(request.getStoreProductId());
        Location location = findLocation(request.getLocationId());
        validateStoreProductReservable(storeProduct);
        validateBookingLocation(storeProduct, location);
        validateStoreProductSlotAvailable(storeProduct.getStoreProductId(), request.getStartAt(), request.getEndAt());

        return Booking.createPendingPaymentForStoreProduct(
                user,
                storeProduct,
                request.getStartAt(),
                request.getEndAt(),
                location,
                request.getLocationText(),
                now.plusMinutes(PAYMENT_HOLD_MINUTES)
        );
    }

    private Location findLocation(Long locationId) {
        if (locationId == null) {
            return null;
        }

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> GeneralException.of(ErrorCode.NOT_FOUND));

        if (!"Y".equals(location.getActiveYn())) {
            throw GeneralException.of(ErrorCode.BAD_REQUEST);
        }

        return location;
    }

    private void validateStoreProductReservable(StoreProduct storeProduct) {
        if (storeProduct.getExpertProfile() == null
                || storeProduct.getExpertProfile().getUser() == null
                || storeProduct.getServiceType() == null
                || storeProduct.getPrice() == null
                || storeProduct.getPrice().signum() < 0
                || storeProduct.getStatus() != StoreProductStatus.ACTIVE
                || storeProduct.getDeletedAt() != null) {
            throw GeneralException.of(ErrorCode.BAD_REQUEST);
        }
    }

    private void validateBookingLocation(
            StoreProduct storeProduct,
            Location bookingLocation
    ) {
        if (bookingLocation == null) {
            if (storeProduct.getServiceType() == StoreProductServiceType.ONLINE) {
                return;
            }

            throw GeneralException.of(ErrorCode.BAD_REQUEST);
        }

        if (storeProduct.getServiceType() == StoreProductServiceType.ONLINE) {
            return;
        }

        Location productLocation = storeProduct.getLocation();

        if (productLocation == null || !"Y".equals(productLocation.getActiveYn())) {
            throw GeneralException.of(ErrorCode.BAD_REQUEST);
        }

        if (!isSameOrChildLocation(bookingLocation, productLocation)) {
            throw GeneralException.of(ErrorCode.BAD_REQUEST);
        }
    }

    private boolean isSameOrChildLocation(
            Location bookingLocation,
            Location productLocation
    ) {
        Location current = bookingLocation;

        while (current != null) {
            if (current.getLocationId().equals(productLocation.getLocationId())) {
                return true;
            }

            current = current.getParent();
        }

        return false;
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
