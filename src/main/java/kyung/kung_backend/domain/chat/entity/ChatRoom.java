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
@Table(
        name = "CHAT_ROOMS",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_CHAT_ROOMS_REQUEST", columnNames = "REQUEST_ID")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "CHAT_ROOMS_SEQ_GENERATOR",
        sequenceName = "CHAT_ROOMS_SEQ",
        allocationSize = 1
)
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CHAT_ROOMS_SEQ_GENERATOR")
    @Column(name = "CHAT_ROOM_ID", nullable = false)
    private Long chatRoomId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "REQUEST_ID", nullable = false)
    private ServiceRequest serviceRequest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "EXPERT_PROFILE_ID", nullable = false)
    private ExpertProfile expertProfile;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;

    @Column(name = "CLOSED_AT")
    private LocalDateTime closedAt;

    public static ChatRoom create(
            ServiceRequest serviceRequest,
            User user,
            ExpertProfile expertProfile
    ) {
        ChatRoom chatRoom = new ChatRoom();

        chatRoom.serviceRequest = serviceRequest;
        chatRoom.user = user;
        chatRoom.expertProfile = expertProfile;
        chatRoom.status = "ACTIVE";
        chatRoom.closedAt = null;

        return chatRoom;
    }

    public void close() {
        this.status = "CLOSED";
        this.closedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return "ACTIVE".equals(this.status);
    }

    public boolean isClosed() {
        return "CLOSED".equals(this.status);
    }
}