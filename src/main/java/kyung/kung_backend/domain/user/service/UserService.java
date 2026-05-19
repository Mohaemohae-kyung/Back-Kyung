package kyung.kung_backend.domain.user.service;

import kyung.kung_backend.domain.user.dto.UserProfileResponse;
import kyung.kung_backend.domain.user.dto.UserProfileUpdateRequest;
import kyung.kung_backend.domain.user.dto.UserWithdrawRequest;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if ("DELETED".equals(user.getStatus())) {
            throw new IllegalArgumentException("탈퇴한 사용자입니다.");
        }
        return user;
    }

    public UserProfileResponse getMyProfile(User currentUser) {
        User user = getUser(currentUser.getUserId());

        return UserProfileResponse.builder()
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .role(user.getRole())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }

    @Transactional
    public UserProfileResponse updateMyProfile(User currentUser, UserProfileUpdateRequest request) {
        User user = getUser(currentUser.getUserId());
        String newProfileImageUrl = user.getProfileImageUrl();

        // 추후 FileUpload 도메인 연동 시 이미지 URL 갱신 로직 추가 필요

        user.updateProfile(request.getName(), request.getPhone(), request.getNickname(), newProfileImageUrl);
        return getMyProfile(user);
    }

    @Transactional
    public void deleteMyAccount(User currentUser, UserWithdrawRequest request) {
        User user = getUser(currentUser.getUserId());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        user.delete();
    }
}