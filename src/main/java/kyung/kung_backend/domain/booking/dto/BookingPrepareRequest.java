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
     * 사용자가 상세 화면에서 예약하려는 서비스 ID입니다.
     * 결제 금액은 프론트가 보낸 값이 아니라 이 서비스의 basePrice를 서버에서 다시 읽어 계산합니다.
     */
    @NotNull(message = "고수 서비스 ID는 필수입니다.")
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
