package kyung.kung_backend.domain.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "마켓 상품 예약 가능 여부 응답")
public class BookingAvailabilityResponse {

    @Schema(description = "선택한 시간에 예약 생성이 가능한지 여부", example = "true")
    private boolean available;

    @Schema(description = "예약 가능 여부 코드. 예약 가능하면 AVAILABLE, 이미 예약된 시간이면 ALREADY_RESERVED입니다.", example = "AVAILABLE")
    private String reason;

    @Schema(description = "예약 가능 여부를 화면에 표시할 때 사용할 수 있는 설명", example = "예약 가능한 시간입니다.")
    private String message;

    public static BookingAvailabilityResponse available() {
        return BookingAvailabilityResponse.builder()
                .available(true)
                .reason("AVAILABLE")
                .message("예약 가능한 시간입니다.")
                .build();
    }

    public static BookingAvailabilityResponse alreadyReserved() {
        return BookingAvailabilityResponse.builder()
                .available(false)
                .reason("ALREADY_RESERVED")
                .message("이미 예약된 시간입니다.")
                .build();
    }
}
