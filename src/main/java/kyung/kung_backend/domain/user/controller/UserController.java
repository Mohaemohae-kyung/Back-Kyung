package kyung.kung_backend.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "User", description = "회원 프로필 및 계정 관리 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "내 프로필 조회",
            description = "로그인한 사용자의 프로필 정보를 조회합니다."
    )
    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> getMyProfile(
            @AuthenticationPrincipal User user
    ) {
        UserProfileResponse response = userService.getMyProfile(user);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @Operation(
            summary = "내 프로필 수정",
            description = "로그인한 사용자의 이름, 닉네임, 전화번호 등 프로필 정보를 수정합니다."
    )
    @PatchMapping("/me")
    public ApiResponse<UserProfileResponse> updateMyProfile(
            @AuthenticationPrincipal User user,
            @RequestBody UserProfileUpdateRequest request
    ) {
        UserProfileResponse response = userService.updateMyProfile(user, request);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @Operation(
            summary = "회원 탈퇴",
            description = "로그인한 사용자의 비밀번호를 확인한 후 회원 상태를 탈퇴 처리합니다."
    )
    @DeleteMapping("/me")
    public ApiResponse<Void> deleteMyAccount(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UserWithdrawRequest request
    ) {
        userService.deleteMyAccount(user, request);
        return ApiResponse.onSuccess(SuccessCode.NO_CONTENT);
    }

    @Operation(
            summary = "FCM 토큰 등록",
            description = "단말의 FCM 토큰을 사용자 계정에 등록합니다. 결제 완료 시 알림 발송에 사용됩니다."
    )
    @PostMapping("/me/fcm-token")
    public ApiResponse<Void> registerFcmToken(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody kyung.kung_backend.domain.user.dto.FcmTokenRequest request
    ) {
        userService.updateFcmToken(user, request.getToken());
        return ApiResponse.onSuccess(SuccessCode.OK);
    }
}