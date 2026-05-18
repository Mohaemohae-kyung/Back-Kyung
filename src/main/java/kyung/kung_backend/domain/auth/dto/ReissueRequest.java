package kyung.kung_backend.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ReissueRequest {

    @NotBlank
    private String refreshToken;
}