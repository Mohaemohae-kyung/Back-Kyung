package kyung.kung_backend.domain.notice.dto;

import kyung.kung_backend.domain.notice.entity.Notice;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class NoticePostResponse {
    private Long postId;
    private String noticeType;
    private String title;
    private String content;
    private Long viewCount;
    private LocalDateTime createdAt;

    public static NoticePostResponse from(Notice notice) {
        return NoticePostResponse.builder()
                .postId(notice.getNoticeId())
                .noticeType(notice.getNoticeType())
                .title(notice.getTitle())
                .content(notice.getContent())
                .viewCount(notice.getViewCount())
                .createdAt(notice.getCreatedAt())
                .build();
    }
}