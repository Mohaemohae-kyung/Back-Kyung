package kyung.kung_backend.domain.chat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatMessageRequest {
    private String roomId;
    private String senderId;
    private String message;
    private String type;
}