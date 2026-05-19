package kyung.kung_backend.domain.mypage.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyPageSummaryResponse {
    private String name;
    private String nickname;
    private String profileImageUrl;
    private long inProgressCount;
    private long bookmarkExpertCount;
    private long postCount;
}