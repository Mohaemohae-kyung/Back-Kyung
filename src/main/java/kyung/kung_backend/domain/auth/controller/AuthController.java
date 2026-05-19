package kyung.kung_backend.domain.auth.controller;

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


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ApiResponse<SignupResponse> signup(
            @Valid @RequestBody SignupRequest request
    ) {
        SignupResponse response = authService.signup(request);
        return ApiResponse.onSuccess(SuccessCode.CREATED, response);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response = authService.login(request);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @PostMapping("/reissue")
    public ApiResponse<ReissueResponse> reissue(
            @Valid @RequestBody ReissueRequest request
    ) {
        ReissueResponse response = authService.reissue(request);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @AuthenticationPrincipal User user
    ) {
        authService.logout(user);
        return ApiResponse.onSuccess(SuccessCode.OK);
    }
}