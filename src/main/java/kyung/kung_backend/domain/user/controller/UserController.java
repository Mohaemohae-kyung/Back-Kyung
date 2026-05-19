package kyung.kung_backend.domain.user.controller;

import jakarta.validation.Valid;
import kyung.kung_backend.domain.user.dto.MyPageSummaryResponse;
import kyung.kung_backend.domain.user.dto.UserProfileResponse;
import kyung.kung_backend.domain.user.dto.UserProfileUpdateRequest;
import kyung.kung_backend.domain.user.dto.UserWithdrawRequest;
import kyung.kung_backend.domain.user.service.UserService;
import kyung.kung_backend.global.response.ApiResponse;
import kyung.kung_backend.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    @GetMapping("/mypage")
    public ApiResponse<MyPageSummaryResponse> getMyPageSummary() {
        Long currentUserId = 1L;
        MyPageSummaryResponse response = userService.getMyPageSummary(currentUserId);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @GetMapping("/users/me")
    public ApiResponse<UserProfileResponse> getMyProfile() {
        Long currentUserId = 1L;
        UserProfileResponse response = userService.getMyProfile(currentUserId);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @PatchMapping("/users/me")
    public ApiResponse<UserProfileResponse> updateMyProfile(
            @RequestBody UserProfileUpdateRequest request) {
        Long currentUserId = 1L;
        UserProfileResponse response = userService.updateMyProfile(currentUserId, request);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @DeleteMapping("/users/me")
    public ApiResponse<Void> withdrawUser(
            @Valid @RequestBody UserWithdrawRequest request) {
        Long currentUserId = 1L;
        userService.withdrawUser(currentUserId, request);
        return ApiResponse.onSuccess(SuccessCode.NO_CONTENT);
    }
}