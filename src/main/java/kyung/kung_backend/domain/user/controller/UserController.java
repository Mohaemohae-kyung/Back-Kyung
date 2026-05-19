package kyung.kung_backend.domain.user.controller;

import jakarta.validation.Valid;
import kyung.kung_backend.domain.user.dto.UserProfileResponse;
import kyung.kung_backend.domain.user.dto.UserProfileUpdateRequest;
import kyung.kung_backend.domain.user.dto.UserWithdrawRequest;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.domain.user.service.UserService;
import kyung.kung_backend.global.response.ApiResponse;
import kyung.kung_backend.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> getMyProfile(
            @AuthenticationPrincipal User user
    ) {
        UserProfileResponse response = userService.getMyProfile(user);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @PatchMapping("/me")
    public ApiResponse<UserProfileResponse> updateMyProfile(
            @AuthenticationPrincipal User user,
            @RequestBody UserProfileUpdateRequest request
    ) {
        UserProfileResponse response = userService.updateMyProfile(user, request);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @DeleteMapping("/me")
    public ApiResponse<Void> deleteMyAccount(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UserWithdrawRequest request
    ) {
        userService.deleteMyAccount(user, request);
        return ApiResponse.onSuccess(SuccessCode.NO_CONTENT);
    }
}