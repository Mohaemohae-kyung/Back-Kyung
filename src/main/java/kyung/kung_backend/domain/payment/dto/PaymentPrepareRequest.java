package kyung.kung_backend.domain.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "결제 준비 요청")
public class PaymentPrepareRequest {

    @Schema(
            description = "결제 대상 타입. 마켓 예약 결제는 BOOKING, 견적 요청 결제는 SERVICE_REQUEST를 사용합니다. 쿠폰은 BOOKING 결제에서만 사용할 수 있습니다.",
            example = "BOOKING",
            allowableValues = {"BOOKING", "SERVICE_REQUEST"},
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "결제 대상 타입은 필수입니다.")
    @Pattern(regexp = "BOOKING|SERVICE_REQUEST", message = "targetType은 BOOKING 또는 SERVICE_REQUEST만 사용할 수 있습니다.")
    private String targetType;

    @Schema(
            description = "결제 대상 ID. targetType=BOOKING이면 bookingId, targetType=SERVICE_REQUEST이면 requestId입니다.",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "결제 대상 ID는 필수입니다.")
    private Long targetId;

    @Schema(description = "결제 수단. PG 연동 전에는 테스트용 문자열로 사용할 수 있습니다.", example = "CARD", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "결제 수단은 필수입니다.")
    private String paymentMethod;

    @Schema(description = "사용할 사용자 쿠폰 ID. 마켓 예약 결제에서만 사용할 수 있으며, 견적 요청 결제는 null 또는 필드 생략", nullable = true, example = "null")
    private Long userCouponId;

    @Schema(description = "PG 제공자 이름. PG 연동 전에는 테스트용 문자열로 사용할 수 있습니다.", example = "TEST_PG")
    private String pgProvider;

    @Schema(description = "결제 서비스명", example = "로고 디자인")
    private String orderName;

    @Schema(description = "모의 해킹용 웰컴 할인 파라미터 (취약점 발현용)", example = "1000")
    private String welcomeDiscountAmount;
}
