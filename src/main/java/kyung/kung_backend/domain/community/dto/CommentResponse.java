package kyung.kung_backend.domain.community.dto;

import kyung.kung_backend.domain.community.entity.CommunityComment;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentResponse {

    private Long commentId;
    private Long postId;
    private String content;
    private String writerName;
    private String status;

    public static CommentResponse from(CommunityComment comment) {
        return CommentResponse.builder()
                .commentId(comment.getCommunityCommentId())
                .postId(comment.getCommunityPost().getCommunityPostId())
                .content(comment.getContent())
                .writerName(comment.getUser().getName())
                .status(comment.getStatus())
                .build();
    }
}