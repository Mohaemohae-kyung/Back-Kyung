package kyung.kung_backend.domain.auth.service;

import kyung.kung_backend.domain.auth.dto.SignupRequest;
import kyung.kung_backend.domain.auth.dto.SignupResponse;
import kyung.kung_backend.domain.auth.dto.LoginRequest;
import kyung.kung_backend.domain.auth.dto.LoginResponse;
import kyung.kung_backend.domain.auth.dto.ReissueRequest;
import kyung.kung_backend.domain.auth.dto.ReissueResponse;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.domain.user.repository.UserRepository;
import kyung.kung_backend.global.response.ErrorCode;
import kyung.kung_backend.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String phone = request.getPhone() == null ? null : request.getPhone().trim();

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    ErrorCode.DUPLICATE_EMAIL.getHttpStatus(),
                    ErrorCode.DUPLICATE_EMAIL.getMessage()
            );
        }

        if (phone != null && !phone.isBlank() && userRepository.existsByPhone(phone)) {
            throw new ResponseStatusException(
                    ErrorCode.DUPLICATE_PHONE.getHttpStatus(),
                    ErrorCode.DUPLICATE_PHONE.getMessage()
            );
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.createUser(
                email,
                encodedPassword,
                request.getName().trim(),
                request.getNickname(),
                phone
        );

        User savedUser = userRepository.save(user);

        return SignupResponse.from(savedUser);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        ErrorCode.INVALID_LOGIN.getHttpStatus(),
                        ErrorCode.INVALID_LOGIN.getMessage()
                ));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(
                    ErrorCode.INVALID_LOGIN.getHttpStatus(),
                    ErrorCode.INVALID_LOGIN.getMessage()
            );
        }

        if ("DELETED".equals(user.getStatus())) {
            throw new ResponseStatusException(
                    ErrorCode.INVALID_LOGIN.getHttpStatus(),
                    "탈퇴한 회원입니다."
            );
        }

        user.updateLastLoginAt();

        String accessToken = jwtProvider.createAccessToken(user);
        String refreshToken = jwtProvider.createRefreshToken(user);

        return LoginResponse.of(user, accessToken, refreshToken);
    }

    @Transactional
    public ReissueResponse reissue(ReissueRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtProvider.validateToken(refreshToken)) {
            throw new ResponseStatusException(
                    ErrorCode.UNAUTHORIZED.getHttpStatus(),
                    "유효하지 않은 Refresh Token입니다."
            );
        }

        Long userId = jwtProvider.getUserId(refreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        ErrorCode.UNAUTHORIZED.getHttpStatus(),
                        "사용자를 찾을 수 없습니다."
                ));

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new ResponseStatusException(
                    ErrorCode.UNAUTHORIZED.getHttpStatus(),
                    "활성 상태의 회원이 아닙니다."
            );
        }

        String newAccessToken = jwtProvider.createAccessToken(user);
        String newRefreshToken = jwtProvider.createRefreshToken(user);

        return ReissueResponse.of(newAccessToken, newRefreshToken);
    }
}