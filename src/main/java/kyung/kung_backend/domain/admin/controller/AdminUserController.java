package kyung.kung_backend.domain.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kyung.kung_backend.domain.admin.dto.AdminUserDetailResponse;
import kyung.kung_backend.domain.admin.dto.AdminUserListResponse;
import kyung.kung_backend.domain.admin.dto.AdminUserSuspendRequest;
import kyung.kung_backend.domain.admin.service.AdminUserService;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin", description = "관리자 API")
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(
            summary = "회원 목록 조회",
            description = "관리자가 전체 회원 목록을 조회합니다."
    )
    @GetMapping
    public ApiResponse<List<AdminUserListResponse>> getUsers() {
        return ApiResponse.onSuccess(adminUserService.getUsers());
    }

    @Operation(
            summary = "회원 상세 조회",
            description = "관리자가 특정 회원의 상세 정보를 조회합니다. 기본 정보, 권한, 계정 상태, 가입일, 고수 여부 등을 확인합니다."
    )
    @GetMapping("/{userId}")
    public ApiResponse<AdminUserDetailResponse> getUserDetail(@PathVariable Long userId) {
        return ApiResponse.onSuccess(adminUserService.getUserDetail(userId));
    }

    @Operation(
            summary = "회원 정지",
            description = "관리자가 특정 회원 계정을 정지 처리합니다. 정지 사유는 관리자 조치 이력에 저장됩니다."
    )
    @PatchMapping("/{userId}/suspend")
    public ApiResponse<String> suspendUser(
            @AuthenticationPrincipal User admin,
            @PathVariable Long userId,
            @RequestBody AdminUserSuspendRequest request
    ) {
        adminUserService.suspendUser(admin.getUserId(), userId, request);

        return ApiResponse.onSuccess("회원 정지가 완료되었습니다.");
    }
}