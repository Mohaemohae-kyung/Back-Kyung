package kyung.kung_backend.domain.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결제 준비 API 요청 DTO입니다.
 *
 * 사용 흐름:
 * 1. 클라이언트가 Swagger 또는 프론트엔드에서 POST /api/payments/prepare 요청 Body로 이 객체 구조의 JSON을 보냅니다.
 * 2. PaymentController.prepare()가 @RequestBody로 이 값을 받습니다.
 * 3. PaymentController는 이 DTO를 PaymentService.prepare()로 넘깁니다.
 * 4. PaymentService는 bookingId를 기준으로 결제 대상과 금액을 계산합니다.
 *
 * 현재는 prepare 스켈레톤 단계이므로 로그인 사용자 정보는 받지 않습니다.
 * 이후 인증 기능이 연결되면 요청 Body가 아니라 SecurityContext에서 로그인 사용자를 꺼내 검증하는 방식으로 확장합니다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "결제 준비 요청")
public class PaymentPrepareRequest {

    /**
     * 결제 대상 매칭 ID입니다.
     *
     * 현재 prepare 저장 기준은 bookingId입니다.
     * matchId는 선택값이며, 함께 보내면 booking.matchId와 일치하는지 검증하는 용도로 사용합니다.
     */
    @Schema(description = "결제 대상 매칭 ID. bookingId와 함께 보내면 예약의 매칭 정보와 일치하는지 검증합니다.", example = "1")
    private Long matchId;

    /**
     * 결제 대상 예약 ID입니다.
     *
     * 현재 prepare 단계의 필수 기준 ID입니다.
     * Booking -> Match -> proposedPrice 흐름으로 결제 금액을 계산합니다.
     */
    @Schema(description = "결제 대상 예약 ID. 현재 prepare API의 필수 기준값입니다.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long bookingId;

    /**
     * 사용자가 선택한 결제 수단입니다.
     *
     * 예시 값:
     * - CARD: 카드 결제
     * - SERVICE_PAY: 서비스 내부 포인트/페이 결제
     *
     * 아직 enum을 만들지 않았기 때문에 문자열로 받습니다.
     * 결제 수단 정책이 확정되면 enum 또는 별도 검증 로직으로 교체할 수 있습니다.
     */
    @NotBlank(message = "결제수단은 필수입니다.")
    @Schema(description = "결제 수단", example = "CARD", allowableValues = {"CARD", "SERVICE_PAY"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String paymentMethod;

    /**
     * Service에서 bookingId 입력 여부를 읽기 좋게 판단하기 위한 편의 메서드입니다.
     */
    public boolean hasBookingId() {
        return bookingId != null;
    }

    /**
     * Service에서 matchId 입력 여부를 읽기 좋게 판단하기 위한 편의 메서드입니다.
     */
    public boolean hasMatchId() {
        return matchId != null;
    }
}
