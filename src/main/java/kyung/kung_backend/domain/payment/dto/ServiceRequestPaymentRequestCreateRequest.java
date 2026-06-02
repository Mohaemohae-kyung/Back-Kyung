package kyung.kung_backend.domain.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ServiceRequestPaymentRequestCreateRequest {

    @NotBlank(message = "결제 수단은 필수입니다.")
    private String paymentMethod;

    @NotBlank(message = "PG 제공자는 필수입니다.")
    private String pgProvider;
}
