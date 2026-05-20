package kyung.kung_backend.domain.chat.dto;

import kyung.kung_backend.domain.chat.entity.ChatMessage;
import kyung.kung_backend.domain.chat.entity.ChatRoom;
import lombok.Builder;
import lombok.Getter;

public class ChatDto {

    @Getter
    @Builder
    public static class MessageResponse {
        private Long chatMessageId;
        private Long roomId;
        private Long senderId;
        private String messageType;
        private String content;
        private String readYn;

        public static MessageResponse from(ChatMessage message) {
            return MessageResponse.builder()
                    .chatMessageId(message.getChatMessageId())
                    .roomId(message.getChatRoom().getChatRoomId())
                    .senderId(message.getSender().getUserId())
                    .messageType(message.getMessageType())
                    .content(message.getContent())
                    .readYn(message.getReadYn())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class RoomResponse {
        private Long chatRoomId;
        private Long userId;

        public static RoomResponse from(ChatRoom room) {
            return RoomResponse.builder()
                    .chatRoomId(room.getChatRoomId())
                    .userId(room.getUser().getUserId())
                    .build();
        }
    }
}