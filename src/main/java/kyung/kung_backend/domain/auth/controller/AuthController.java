package kyung.kung_backend.domain.auth.controller;

import jakarta.validation.Valid;
import kyung.kung_backend.domain.auth.dto.SignupRequest;
import kyung.kung_backend.domain.auth.dto.SignupResponse;
import kyung.kung_backend.domain.auth.dto.LoginRequest;
import kyung.kung_backend.domain.auth.dto.LoginResponse;
import kyung.kung_backend.domain.auth.dto.ReissueRequest;
import kyung.kung_backend.domain.auth.dto.ReissueResponse;
import kyung.kung_backend.domain.auth.service.AuthService;
import kyung.kung_backend.global.response.ApiResponse;
import kyung.kung_backend.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(
            @Valid @RequestBody SignupRequest request
    ) {
        SignupResponse response = authService.signup(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.onSuccess(SuccessCode.CREATED, response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response = authService.login(request);

        return ResponseEntity
                .ok(ApiResponse.onSuccess(SuccessCode.OK, response));
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<ReissueResponse>> reissue(
            @Valid @RequestBody ReissueRequest request
    ) {
        ReissueResponse response = authService.reissue(request);

        return ResponseEntity.ok(
                ApiResponse.onSuccess(SuccessCode.OK, response)
        );
    }
}