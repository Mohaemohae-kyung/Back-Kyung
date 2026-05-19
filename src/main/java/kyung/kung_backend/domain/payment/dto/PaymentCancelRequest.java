package kyung.kung_backend.domain.payment.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentCancelRequest {

    /*
     * 결제 취소/환불 사유입니다.
     * PG 취소 요청을 붙일 때도 같은 사유를 PG에 넘기고 관리자 확인용으로 DB에 남깁니다.
     */
    @Size(max = 500, message = "취소 사유는 500자 이하로 입력해주세요.")
    private String reason;
}
