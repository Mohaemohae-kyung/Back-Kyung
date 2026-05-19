package kyung.kung_backend.domain.community.dto;

import kyung.kung_backend.domain.community.entity.CommunityPost;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostResponse {

    private Long postId;
    private String title;
    private String content;
    private Long viewCount;
    private String writerName;

    public static PostResponse from(CommunityPost post) {
        return PostResponse.builder()
                .postId(post.getCommunityPostId())
                .title(post.getTitle())
                .content(post.getContent())
                .viewCount(post.getViewCount())
                .writerName(post.getUser().getName())
                .build();
    }
}