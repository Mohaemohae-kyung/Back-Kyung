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

        private Long paymentId;

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
                            message.getSender() != null
                                    ? message.getSender().getUserId()
                                    : null
                    )

                    .messageType(
                            message.getMessageType()
                    )

                    .paymentId(
                            message.getPaymentId()
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
        // 연결된 서비스 요청 ID
        // 결제 요청 시 사용
        // =========================
        private Long serviceRequestId;

        // 요청 사용자 닉네임
        private String requestUserNickname;

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

                    // 서비스 요청 ID
                    .serviceRequestId(
                            room.getServiceRequest() != null
                                    ? room.getServiceRequest().getRequestId()
                                    : null
                    )

                    // 요청 사용자 닉네임
                    .requestUserNickname(
                            room.getUser()
                                    .getNickname()
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