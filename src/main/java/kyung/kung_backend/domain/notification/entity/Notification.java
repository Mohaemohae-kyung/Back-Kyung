package kyung.kung_backend.domain.notification.entity;

import jakarta.persistence.*;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.common.BaseCreatedEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "NOTIFICATIONS")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "NOTIFICATIONS_SEQ_GENERATOR",
        sequenceName = "NOTIFICATIONS_SEQ",
        allocationSize = 1
)
public class Notification extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "NOTIFICATIONS_SEQ_GENERATOR")
    @Column(name = "NOTIFICATION_ID", nullable = false)
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Column(name = "NOTIFICATION_TYPE", nullable = false, length = 30)
    private String notificationType;

    @Column(name = "TITLE", nullable = false, length = 200)
    private String title;

    @Column(name = "CONTENT", length = 1000)
    private String content;

    @Column(name = "LINK_URL", length = 500)
    private String linkUrl;

    @Column(name = "READ_YN", nullable = false, length = 1)
    private String readYn;

    @Column(name = "READ_AT")
    private LocalDateTime readAt;
}