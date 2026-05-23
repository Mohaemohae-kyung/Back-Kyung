package kyung.kung_backend.domain.chat.service;

import kyung.kung_backend.domain.chat.dto.ChatDto;
import kyung.kung_backend.domain.chat.dto.ChatMessageRequest;
import kyung.kung_backend.domain.chat.entity.ChatMessage;
import kyung.kung_backend.domain.chat.entity.ChatRoom;
import kyung.kung_backend.domain.chat.repository.ChatMessageRepository;
import kyung.kung_backend.domain.chat.repository.ChatRoomRepository;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatRoomRepository
            chatRoomRepository;

    private final ChatMessageRepository
            chatMessageRepository;

    private final UserRepository
            userRepository;

    // =========================
    // 메시지 저장
    // =========================
    @Transactional
    public ChatDto.MessageResponse saveMessage(
            ChatMessageRequest request
    ) {

        ChatRoom room =

                chatRoomRepository.findById(

                        Long.valueOf(
                                request.getRoomId()
                        )

                ).orElseThrow(() ->

                        new IllegalArgumentException(
                                "채팅방 없음"
                        )
                );

        User sender =

                userRepository.findById(

                        Long.valueOf(
                                request.getSenderId()
                        )

                ).orElseThrow(() ->

                        new IllegalArgumentException(
                                "유저 없음"
                        )
                );

        ChatMessage message =

                ChatMessage.create(

                        room,

                        sender,

                        request.getType(),

                        request.getMessage()
                );

        ChatMessage saved =

                chatMessageRepository.save(
                        message
                );

        return ChatDto.MessageResponse.from(
                saved
        );
    }

    // =========================
    // 내 채팅방 목록 조회
    // =========================
    public List<ChatDto.RoomResponse>
    getMyRoomsAsUser(Long userId) {

        return chatRoomRepository

                .findMyRooms(userId)

                .stream()

                .map(room -> {

                    // =========================
                    // 안 읽은 메시지 수
                    // =========================
                    Long unreadCount =

                            chatMessageRepository
                                    .countUnread(

                                            room.getChatRoomId(),

                                            userId
                                    );

                    // =========================
                    // 마지막 메시지
                    // =========================
                    String lastMessage =

                            chatMessageRepository

                                    .findTopByChatRoomOrderByChatMessageIdDesc(
                                            room
                                    )

                                    .map(
                                            ChatMessage::getContent
                                    )

                                    .orElse(null);

                    return ChatDto.RoomResponse.from(

                            room,

                            lastMessage,

                            unreadCount
                    );
                })

                .collect(Collectors.toList());
    }

    // =========================
    // 채팅 메시지 조회
    // =========================
    public List<ChatDto.MessageResponse>
    getRoomMessages(Long roomId) {

        return chatMessageRepository

                .findByChatRoomChatRoomIdOrderByCreatedAtAsc(
                        roomId
                )

                .stream()

                .map(
                        ChatDto.MessageResponse::from
                )

                .collect(Collectors.toList());
    }

    // =========================
    // 채팅방 조회
    // =========================
    public ChatRoom getRoom(Long roomId) {

        return chatRoomRepository

                .findById(roomId)

                .orElseThrow(() ->

                        new IllegalArgumentException(
                                "채팅방 없음"
                        )
                );
    }

    // =========================
    // 읽음 처리
    // =========================
    @Transactional
    public void readMessages(

            Long roomId,

            Long userId
    ) {

        List<ChatMessage> messages =

                chatMessageRepository
                        .findUnreadMessages(

                                roomId,

                                userId
                        );

        messages.forEach(
                ChatMessage::read
        );
    }
}