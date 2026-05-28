package kyung.kung_backend.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class E2ePayloadRequest {
    
    @JsonProperty("encryptedAesKey")
    private String encryptedAesKey;
    
    @JsonProperty("iv")
    private String iv;
    
    @JsonProperty("cipherText")
    private String cipherText;
}
