package kyung.kung_backend.domain.chat.repository;

import kyung.kung_backend.domain.chat.entity.ChatMessage;
import kyung.kung_backend.domain.chat.entity.ChatRoom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository
        extends JpaRepository<ChatMessage, Long> {

    // =========================
    // 채팅방 메시지 조회
    // =========================
    List<ChatMessage>
    findByChatRoomChatRoomIdOrderByCreatedAtAsc(
            Long chatRoomId
    );

    // =========================
    // 마지막 메시지 조회
    // =========================
    Optional<ChatMessage>
    findTopByChatRoomOrderByChatMessageIdDesc(
            ChatRoom chatRoom
    );

    // =========================
    // 읽지 않은 메시지 개수
    // =========================
    @Query("""
        select count(m)
        from ChatMessage m
        where m.chatRoom.chatRoomId = :roomId
          and m.sender.userId <> :userId
          and m.readYn = 'N'
    """)
    Long countUnread(
            Long roomId,
            Long userId
    );

    // =========================
    // 읽지 않은 메시지 목록
    // =========================
    @Query("""
        select m
        from ChatMessage m
        where m.chatRoom.chatRoomId = :roomId
          and m.sender.userId <> :userId
          and m.readYn = 'N'
    """)
    List<ChatMessage> findUnreadMessages(
            Long roomId,
            Long userId
    );
}