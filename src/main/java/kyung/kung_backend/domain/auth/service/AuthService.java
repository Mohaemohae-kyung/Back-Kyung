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
import kyung.kung_backend.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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
            throw GeneralException.of(ErrorCode.DUPLICATE_EMAIL);
        }

        if (phone != null && !phone.isBlank() && userRepository.existsByPhone(phone)) {
            throw GeneralException.of(ErrorCode.DUPLICATE_PHONE);
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
                .orElseThrow(() -> GeneralException.of(ErrorCode.INVALID_LOGIN));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw GeneralException.of(ErrorCode.INVALID_LOGIN);
        }

        if ("DELETED".equals(user.getStatus())) {
            throw GeneralException.of(ErrorCode.DELETED_USER);
        }

        user.updateLastLoginAt();

        String accessToken = jwtProvider.createAccessToken(user);
        String refreshToken = jwtProvider.createRefreshToken(user);

        user.updateRefreshToken(refreshToken);

        return LoginResponse.of(user, accessToken, refreshToken);
    }

    @Transactional
    public ReissueResponse reissue(ReissueRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtProvider.validateToken(refreshToken)) {
            throw GeneralException.of(ErrorCode.UNAUTHORIZED);
        }

        Long userId = jwtProvider.getUserId(refreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> GeneralException.of(ErrorCode.UNAUTHORIZED));

        if (!"ACTIVE".equals(user.getStatus())) {
            throw GeneralException.of(ErrorCode.UNAUTHORIZED);
        }

        if (user.getRefreshToken() == null || !user.getRefreshToken().equals(refreshToken)) {
            throw GeneralException.of(ErrorCode.UNAUTHORIZED);
        }

        String newAccessToken = jwtProvider.createAccessToken(user);
        String newRefreshToken = jwtProvider.createRefreshToken(user);

        user.updateRefreshToken(newRefreshToken);

        return ReissueResponse.of(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(User user) {
        User findUser = userRepository.findById(user.getUserId())
                .orElseThrow(() -> GeneralException.of(ErrorCode.UNAUTHORIZED));

        findUser.clearRefreshToken();
    }
}