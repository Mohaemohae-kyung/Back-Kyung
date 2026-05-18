package kyung.kung_backend.domain.user.controller;

import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.domain.user.service.UserService;
import kyung.kung_backend.global.response.ApiResponse;
import kyung.kung_backend.global.response.SuccessCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<String>> getMyInfo(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(
                ApiResponse.onSuccess(SuccessCode.OK, user.getEmail())
        );
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<String>> deleteMyAccount(
            @AuthenticationPrincipal User user
    ) {
        userService.deleteMyAccount(user);

        return ResponseEntity.ok(
                ApiResponse.onSuccess(SuccessCode.OK, "회원탈퇴가 완료되었습니다.")
        );
    }
}