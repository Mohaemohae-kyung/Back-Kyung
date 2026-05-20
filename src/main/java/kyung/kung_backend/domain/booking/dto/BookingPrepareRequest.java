package kyung.kung_backend.domain.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Schema(description = "마켓 상품 예약 생성 요청")
public class BookingPrepareRequest {

    @Schema(description = "예약할 마켓 상품 ID", example = "21", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "마켓 상품 ID는 필수입니다.")
    private Long storeProductId;

    @Schema(description = "예약 시작 일시. LocalDateTime 형식으로 전달합니다.", example = "2026-05-21T10:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "예약 시작 시간은 필수입니다.")
    private LocalDateTime startAt;

    @Schema(description = "예약 종료 일시. 시간 충돌 검사를 위해 시작/종료 시간이 모두 필요합니다.", example = "2026-05-21T11:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "예약 종료 시간은 필수입니다.")
    private LocalDateTime endAt;

    @Schema(description = "LOCATIONS 테이블의 위치 ID. 오프라인/BOTH 상품은 고수의 활동 지역과 같거나 그 하위 지역이어야 합니다.", example = "444")
    private Long locationId;

    @Schema(description = "상세 장소, 주소 보충 정보, 온라인 진행 링크 등 선택 입력값", example = "서울 강남구 테헤란로 123")
    @Size(max = 255, message = "장소 정보는 255자 이하로 입력해주세요.")
    private String locationText;
}
