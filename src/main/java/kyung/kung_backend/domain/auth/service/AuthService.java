package kyung.kung_backend.domain.auth.service;

import kyung.kung_backend.domain.auth.dto.*;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.domain.user.repository.UserRepository;
import kyung.kung_backend.global.exception.GeneralException;
import kyung.kung_backend.global.jwt.JwtProvider;
import kyung.kung_backend.global.response.ErrorCode;
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

    @Transactional
    public void changePassword(User user, PasswordChangeRequest request) {
        // 영속성 컨텍스트에서 현재 로그인한 유저를 다시 조회
        User findUser = userRepository.findById(user.getUserId())
                .orElseThrow(() -> GeneralException.of(ErrorCode.UNAUTHORIZED));

        // 1. 현재 입력한 비밀번호와 DB의 암호화된 비밀번호 대조
        if (!passwordEncoder.matches(request.getCurrentPassword(), findUser.getPassword())) {
            // 기존 ErrorCode 명세 중 알맞은 인증 실패 코드를 매핑합니다.
            throw GeneralException.of(ErrorCode.INVALID_LOGIN);
        }

        // 2. 새로운 비밀번호를 암호화하여 엔티티 도메인 메서드로 주입
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        findUser.updatePassword(encodedPassword);

        // @Transactional에 의해 메서드 종료 시 영속성 데이터가 자동으로 Dirty Checking(변경 감지)되어 DB에 반영됩니다.
    }
}