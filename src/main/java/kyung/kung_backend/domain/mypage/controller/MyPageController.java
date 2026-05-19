package kyung.kung_backend.domain.mypage.controller;

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

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping
    public ApiResponse<MyPageSummaryResponse> getMyPageSummary(
            @AuthenticationPrincipal User user
    ) {
        MyPageSummaryResponse response = myPageService.getMyPageSummary(user);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }
}