package kyung.kung_backend.domain.community.entity;

import jakarta.persistence.*;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "COMMUNITY_COMMENTS")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "COMMUNITY_COMMENTS_SEQ_GENERATOR",
        sequenceName = "COMMUNITY_COMMENTS_SEQ",
        allocationSize = 1
)
public class CommunityComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "COMMUNITY_COMMENTS_SEQ_GENERATOR")
    @Column(name = "COMMUNITY_COMMENT_ID", nullable = false)
    private Long communityCommentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "COMMUNITY_POST_ID", nullable = false)
    private CommunityPost communityPost;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_ID")
    private CommunityComment parent;

    @Lob
    @Column(name = "CONTENT", nullable = false)
    private String content;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;

    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;
}