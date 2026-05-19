package kyung.kung_backend.domain.booking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class BookingPrepareRequest {

    /*
     * 숨고 마켓 상품 ID입니다.
     * 마켓 결제 흐름은 StoreProduct를 기준으로 예약을 만들고 결제 금액은 StoreProduct.price에서 계산합니다.
     */
    private Long storeProductId;

    /*
     * 기존 서비스 게시글 기반 테스트를 위한 값입니다.
     * 실제 마켓 플로우에서는 storeProductId를 우선 사용합니다.
     */
    private Long expertServiceId;

    /*
     * 사용자가 선택한 예약 시작/종료 시각입니다.
     * 서비스 계층에서 endAt > startAt, 과거 예약 방지, 기존 예약과의 시간 충돌을 검증합니다.
     */
    @NotNull(message = "예약 시작 시간은 필수입니다.")
    private LocalDateTime startAt;

    @NotNull(message = "예약 종료 시간은 필수입니다.")
    private LocalDateTime endAt;

    /*
     * 오프라인 진행 장소나 온라인 링크 안내용 텍스트입니다.
     * 아직 별도 장소 테이블을 만들지 않았으므로 예약에 스냅샷 문자열로 저장합니다.
     */
    @Size(max = 255, message = "장소 정보는 255자 이하로 입력해주세요.")
    private String locationText;
}
