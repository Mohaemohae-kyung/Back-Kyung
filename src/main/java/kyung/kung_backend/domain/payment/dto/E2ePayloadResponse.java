package kyung.kung_backend.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class E2ePayloadResponse {
    private boolean success;
    private String cipherText;
    private String message;
}
