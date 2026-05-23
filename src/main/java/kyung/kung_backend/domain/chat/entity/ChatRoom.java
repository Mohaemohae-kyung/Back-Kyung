package kyung.kung_backend.domain.chat.entity;

import jakarta.persistence.*;
import kyung.kung_backend.domain.expert.entity.ExpertProfile;
import kyung.kung_backend.domain.request.entity.ServiceRequest;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "CHAT_ROOMS")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CHAT_ROOM_ID")
    private Long chatRoomId;

    // =========================
    // 채팅방 이름
    // =========================
    @Column(name = "ROOM_NAME", nullable = false)
    private String roomName;

    // =========================
    // 요청 정보
    // =========================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REQUEST_ID", nullable = false)
    private ServiceRequest serviceRequest;

    // =========================
    // 요청 사용자
    // =========================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    // =========================
    // 고수 프로필
    // =========================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EXPERT_PROFILE_ID", nullable = false)
    private ExpertProfile expertProfile;

    // =========================
    // 상태
    // =========================
    @Column(name = "STATUS")
    private String status;

    // =========================
    // 마지막 메시지 시간
    // =========================
    @Column(name = "LAST_MESSAGE_AT")
    private LocalDateTime lastMessageAt;

    // =========================
    // 채팅방 생성
    // =========================
    public static ChatRoom create(
            ServiceRequest serviceRequest,
            User user,
            ExpertProfile expertProfile
    ) {

        ChatRoom chatRoom =
                new ChatRoom();

        // 요청 정보
        chatRoom.serviceRequest =
                serviceRequest;

        // 요청 제목을 채팅방 이름으로 사용
        chatRoom.roomName =
                serviceRequest.getTitle();

        // 요청 사용자
        chatRoom.user =
                user;

        // 고수 프로필
        chatRoom.expertProfile =
                expertProfile;

        // 상태
        chatRoom.status =
                "ACTIVE";

        // 마지막 메시지 시간
        chatRoom.lastMessageAt =
                LocalDateTime.now();

        return chatRoom;
    }

    // =========================
    // 마지막 메시지 시간 갱신
    // =========================
    public void updateLastMessageAt() {

        this.lastMessageAt =
                LocalDateTime.now();
    }
}