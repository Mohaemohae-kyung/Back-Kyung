package kyung.kung_backend.domain.mypage.service;

import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(
            summary = "마이페이지 요약 통계 정보 빌드",
            description = "현재 요청한 회원의 기본 인적 데이터를 조회하고 서비스 내 활동 집계 데이터를 결합하여 마이페이지 메인 화면 뷰에 맞는 데이터를 생성합니다."
    )
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