package kyung.kung_backend.domain.mypage.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import kyung.kung_backend.domain.mypage.dto.MyPageSummaryResponse;
import kyung.kung_backend.domain.mypage.service.MyPageService;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.response.ApiResponse;
import kyung.kung_backend.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "MyPage", description = "마이페이지 및 활동 정보 API")
@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    @Operation(
            summary = "마이페이지 조회",
            description = "로그인한 사용자의 프로필 및 활동 요약 정보를 조회합니다."
    )
    @GetMapping
    public ApiResponse<MyPageSummaryResponse> getMyPageSummary(
            @AuthenticationPrincipal User user
    ) {
        MyPageSummaryResponse response = myPageService.getMyPageSummary(user);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }
}