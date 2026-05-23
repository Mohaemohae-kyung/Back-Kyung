package kyung.kung_backend.domain.chat.dto;

import kyung.kung_backend.domain.chat.entity.ChatMessage;
import kyung.kung_backend.domain.chat.entity.ChatRoom;

import lombok.Builder;
import lombok.Getter;

public class ChatDto {

    // =========================
    // 메시지 응답 DTO
    // =========================
    @Getter
    @Builder
    public static class MessageResponse {

        private Long chatMessageId;

        private Long roomId;

        private Long senderId;

        private String messageType;

        private String content;

        private String readYn;

        public static MessageResponse from(
                ChatMessage message
        ) {

            return MessageResponse.builder()

                    .chatMessageId(
                            message.getChatMessageId()
                    )

                    .roomId(
                            message.getChatRoom()
                                    .getChatRoomId()
                    )

                    .senderId(
                            message.getSender()
                                    .getUserId()
                    )

                    .messageType(
                            message.getMessageType()
                    )

                    .content(
                            message.getContent()
                    )

                    .readYn(
                            message.getReadYn()
                    )

                    .build();
        }
    }

    // =========================
    // 채팅방 응답 DTO
    // =========================
    @Getter
    @Builder
    public static class RoomResponse {

        // =========================
        // 채팅방 ID
        // =========================
        private Long chatRoomId;

        // =========================
        // 채팅방 이름
        // =========================
        private String roomName;

        // =========================
        // 요청 사용자 ID
        // =========================
        private Long userId;

        // =========================
        // 마지막 메시지
        // =========================
        private String lastMessage;

        // =========================
        // 읽지 않은 메시지 수
        // =========================
        private Long unreadCount;

        public static RoomResponse from(
                ChatRoom room,
                String lastMessage,
                Long unreadCount
        ) {

            return RoomResponse.builder()

                    // 채팅방 번호
                    .chatRoomId(
                            room.getChatRoomId()
                    )

                    // 채팅방 이름
                    .roomName(
                            room.getRoomName()
                    )

                    // 요청 사용자 ID
                    .userId(
                            room.getUser()
                                    .getUserId()
                    )

                    // 마지막 메시지
                    .lastMessage(
                            lastMessage
                    )

                    // 안 읽은 메시지 수
                    .unreadCount(
                            unreadCount
                    )

                    .build();
        }
    }
}