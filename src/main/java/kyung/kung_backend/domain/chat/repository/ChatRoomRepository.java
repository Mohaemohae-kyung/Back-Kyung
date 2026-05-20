package kyung.kung_backend.domain.chat.repository;

import kyung.kung_backend.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    List<ChatRoom> findByUserUserId(Long userId);
    List<ChatRoom> findByExpertProfileExpertProfileId(Long expertProfileId);
}