package kyung.kung_backend.domain.booking.controller;

import jakarta.validation.Valid;
import kyung.kung_backend.domain.booking.dto.BookingPrepareRequest;
import kyung.kung_backend.domain.booking.dto.BookingResponse;
import kyung.kung_backend.domain.booking.service.BookingService;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.response.ApiResponse;
import kyung.kung_backend.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    /*
     * 날짜/시간 선택 후 "예약하기" 버튼을 눌렀을 때 호출하는 API입니다.
     * 아직 돈이 결제된 상태가 아니므로 예약 상태는 PENDING_PAYMENT로 생성됩니다.
     */
    @PostMapping("/prepare")
    public ResponseEntity<ApiResponse<BookingResponse>> prepareBooking(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody BookingPrepareRequest request
    ) {
        BookingResponse response = bookingService.prepareBooking(user, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.onSuccess(SuccessCode.CREATED, response));
    }

    /*
     * 로그인 사용자의 예약 목록입니다.
     * 결제 전 임시 예약, 확정 예약, 취소/만료 예약을 모두 내려주고 상태값으로 화면을 분기합니다.
     */
    @GetMapping("/me")
    public ApiResponse<List<BookingResponse>> getMyBookings(
            @AuthenticationPrincipal User user
    ) {
        return ApiResponse.onSuccess(SuccessCode.OK, bookingService.getMyBookings(user));
    }

    /*
     * 예약 상세 조회입니다.
     * 결제 화면 이동 전 예약 시간이 맞는지 확인하거나, 결제 완료 후 예약 상세 화면에서 사용합니다.
     */
    @GetMapping("/{bookingId}")
    public ApiResponse<BookingResponse> getBookingDetail(
            @AuthenticationPrincipal User user,
            @PathVariable Long bookingId
    ) {
        return ApiResponse.onSuccess(SuccessCode.OK, bookingService.getBookingDetail(user, bookingId));
    }
}
