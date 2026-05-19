package kyung.kung_backend.domain.notice.entity;

import jakarta.persistence.*;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "NOTICES")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "NOTICES_SEQ_GENERATOR",
        sequenceName = "NOTICES_SEQ",
        allocationSize = 1
)
public class Notice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "NOTICES_SEQ_GENERATOR")
    @Column(name = "NOTICE_ID", nullable = false)
    private Long noticeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ADMIN_ID", nullable = false)
    private User admin;

    @Column(name = "NOTICE_TYPE", nullable = false, length = 30)
    private String noticeType;

    @Column(name = "TITLE", nullable = false, length = 200)
    private String title;

    @Lob
    @Column(name = "CONTENT", nullable = false)
    private String content;

    @Column(name = "VIEW_COUNT", nullable = false)
    private Long viewCount;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;

    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    public static Notice createNotice(User admin, String noticeType, String title, String content) {
        Notice notice = new Notice();
        notice.admin = admin;
        notice.noticeType = noticeType;
        notice.title = title;
        notice.content = content;
        notice.viewCount = 0L;
        notice.status = "ACTIVE";
        return notice;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void updateNotice(String title, String content) {
        if (title != null && !title.trim().isEmpty()) {
            this.title = title;
        }
        if (content != null && !content.trim().isEmpty()) {
            this.content = content;
        }
    }

    // 기존 클래스 내부 최하단(닫는 중괄호 바로 위)에 추가합니다.
    public void updateStatus(String status) {
        this.status = status;
    }
}