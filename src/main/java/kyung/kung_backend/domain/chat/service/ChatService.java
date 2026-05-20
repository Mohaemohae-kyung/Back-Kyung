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

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChatDto.MessageResponse saveMessage(ChatMessageRequest request) {
        ChatRoom room = chatRoomRepository.findById(Long.valueOf(request.getRoomId()))
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다. ID: " + request.getRoomId()));

        User sender = userRepository.findById(Long.valueOf(request.getSenderId()))
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + request.getSenderId()));

        ChatMessage message = ChatMessage.create(room, sender, request.getType(), request.getMessage());

        ChatMessage savedMessage = chatMessageRepository.save(message);

        return ChatDto.MessageResponse.from(savedMessage);
    }

    public List<ChatDto.RoomResponse> getMyRoomsAsUser(Long userId) {
        List<ChatRoom> rooms = chatRoomRepository.findByUserUserId(userId);

        return rooms.stream()
                .map(ChatDto.RoomResponse::from)
                .collect(Collectors.toList());
    }

    public List<ChatDto.MessageResponse> getRoomMessages(Long chatRoomId) {
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomChatRoomIdOrderByCreatedAtAsc(chatRoomId);

        return messages.stream()
                .map(ChatDto.MessageResponse::from)
                .collect(Collectors.toList());
    }
}