package kyung.kung_backend.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class E2ePayloadRequest {
    private String encryptedAesKey;
    private String iv;
    private String cipherText;
}
