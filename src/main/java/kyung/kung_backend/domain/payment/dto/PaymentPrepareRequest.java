package kyung.kung_backend.domain.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentPrepareRequest {

    /*
     * 결제 대상 종류입니다.
     * BOOKING은 이미 생성된 예약을 결제할 때 사용하고,
     * SERVICE_REQUEST 또는 REQUEST는 승인된 견적 요청을 결제 준비 단계에서 바로 결제할 때 사용합니다.
     */
    @NotBlank(message = "결제 대상 타입은 필수입니다.")
    private String targetType;

    /*
     * targetType이 BOOKING이면 bookingId, SERVICE_REQUEST이면 requestId입니다.
     * 서버는 이 ID로 대상 데이터를 조회하고 로그인 사용자 소유 여부와 결제 가능 상태를 검증합니다.
     */
    @NotNull(message = "결제 대상 ID는 필수입니다.")
    private Long targetId;

    /*
     * CARD, EASY_PAY, VIRTUAL_ACCOUNT, SERVICE_PAY 등 결제 수단을 담습니다.
     * PG사별 실제 결제 수단 코드는 이후 PG 연동 계층에서 매핑합니다.
     */
    @NotBlank(message = "결제 수단은 필수입니다.")
    private String paymentMethod;

    /*
     * 사용할 쿠폰이 없으면 null입니다.
     * 쿠폰 할인은 프론트 계산값을 받지 않고 서버에서 쿠폰 정책을 조회해 다시 계산합니다.
     */
    private Long userCouponId;

    /*
     * 토스페이먼츠, 카카오페이 등 실제 PG 제공자 이름을 기록하는 선택값입니다.
     * 아직 실제 PG 호출을 붙이지 않았으므로 기록/응답 확인용으로만 사용합니다.
     */
    private String pgProvider;
}
