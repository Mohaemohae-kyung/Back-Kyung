package kyung.kung_backend.domain.auth.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import kyung.kung_backend.domain.auth.dto.SignupRequest;
import kyung.kung_backend.domain.auth.dto.SignupResponse;
import kyung.kung_backend.domain.auth.dto.LoginRequest;
import kyung.kung_backend.domain.auth.dto.LoginResponse;
import kyung.kung_backend.domain.auth.dto.ReissueRequest;
import kyung.kung_backend.domain.auth.dto.ReissueResponse;
import kyung.kung_backend.domain.auth.service.AuthService;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.response.ApiResponse;
import kyung.kung_backend.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "Auth", description = "회원 인증 및 토큰 관리 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "회원가입",
            description = "신규 사용자의 계정을 생성하고 비밀번호를 암호화하여 저장합니다."
    )
    @PostMapping("/signup")
    public ApiResponse<SignupResponse> signup(
            @Valid @RequestBody SignupRequest request
    ) {
        SignupResponse response = authService.signup(request);
        return ApiResponse.onSuccess(SuccessCode.CREATED, response);
    }

    @Operation(
            summary = "로그인",
            description = "이메일과 비밀번호를 검증한 후 Access Token과 Refresh Token을 발급합니다."
    )
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response = authService.login(request);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @Operation(
            summary = "토큰 재발급",
            description = "Refresh Token을 검증한 후 새로운 Access Token과 Refresh Token을 발급합니다."
    )
    @PostMapping("/reissue")
    public ApiResponse<ReissueResponse> reissue(
            @Valid @RequestBody ReissueRequest request
    ) {
        ReissueResponse response = authService.reissue(request);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @Operation(
            summary = "로그아웃",
            description = "로그인한 사용자의 Refresh Token을 삭제하여 토큰 재발급을 차단합니다."
    )
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @AuthenticationPrincipal User user
    ) {
        authService.logout(user);
        return ApiResponse.onSuccess(SuccessCode.OK);
    }
}