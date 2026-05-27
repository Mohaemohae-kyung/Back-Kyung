package kyung.kung_backend.domain.chat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class AIChatbotDto {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Request {
        private String message;
        private String session_id;
        private String mode;
        private Long user_id;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Response {
        private String reply;
    }
}
