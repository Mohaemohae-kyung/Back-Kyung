package kyung.kung_backend.domain.mypage.service;

import kyung.kung_backend.domain.mypage.dto.MyPageSummaryResponse;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final UserService userService;

    public MyPageSummaryResponse getMyPageSummary(User currentUser) {
        User user = userService.getUser(currentUser.getUserId());

        return MyPageSummaryResponse.builder()
                .name(user.getName())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .inProgressCount(0L)
                .bookmarkExpertCount(0L)
                .postCount(0L)
                .build();
    }
}