package kyung.kung_backend.domain.notice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class NoticePostCreateRequest {
    @NotBlank(message = "게시글 유형은 필수입니다.")
    private String type;
    @NotBlank(message = "제목은 필수입니다.")
    private String title;
    @NotBlank(message = "본문은 필수입니다.")
    private String content;
    private List<Long> attachmentFileIds;
    private boolean isPinned;
}