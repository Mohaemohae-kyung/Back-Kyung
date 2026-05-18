package kyung.kung_backend.global.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Builder
public class ReasonDto {

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

}