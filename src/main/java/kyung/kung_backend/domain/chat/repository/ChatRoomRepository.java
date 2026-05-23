package kyung.kung_backend.domain.chat.repository;

import kyung.kung_backend.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository
        extends JpaRepository<ChatRoom, Long> {

    // =========================
    // 내가 참여한 채팅방 목록 조회
    // =========================
    @Query("""
        select cr
        from ChatRoom cr
        where cr.user.userId = :userId
           or cr.expertProfile.user.userId = :userId
        order by cr.chatRoomId asc
    """)
    List<ChatRoom> findMyRooms(
            Long userId
    );

    // =========================
    // 요청 ID로 채팅방 조회
    // =========================
    Optional<ChatRoom>
    findByServiceRequest_RequestId(
            Long requestId
    );
}