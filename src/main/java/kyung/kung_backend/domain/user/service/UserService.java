package kyung.kung_backend.domain.user.service;

import kyung.kung_backend.domain.user.dto.MyPageSummaryResponse;
import kyung.kung_backend.domain.user.dto.UserProfileResponse;
import kyung.kung_backend.domain.user.dto.UserProfileUpdateRequest;
import kyung.kung_backend.domain.user.dto.UserWithdrawRequest;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // 파일 도메인 활성화 시 주석 해제 필요
    // private final FileUploadRepository fileUploadRepository;

    @Transactional(readOnly = true)
    public MyPageSummaryResponse getMyPageSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if ("DELETED".equals(user.getStatus())) {
            throw new IllegalArgumentException("탈퇴한 사용자입니다.");
        }

        return MyPageSummaryResponse.builder()
                .name(user.getName())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .inProgressCount(0L)
                .bookmarkExpertCount(0L)
                .postCount(0L)
                .build();
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if ("DELETED".equals(user.getStatus())) {
            throw new IllegalArgumentException("탈퇴한 사용자입니다.");
        }

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
    public UserProfileResponse updateMyProfile(Long userId, UserProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if ("DELETED".equals(user.getStatus())) {
            throw new IllegalArgumentException("탈퇴한 사용자입니다.");
        }

        String newProfileImageUrl = null;

        // 파일 API 보류 해제 시 구현될 로직 예시
        if (request.getProfileImageFileId() != null) {
            // FileUpload file = fileUploadRepository.findById(request.getProfileImageFileId())
            //         .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다."));
            // file.updateTarget("USER_PROFILE", user.getUserId());
            // newProfileImageUrl = file.getFileUrl();
        }

        user.updateProfile(request.getName(), request.getPhone(), request.getNickname(), newProfileImageUrl);

        return getMyProfile(userId);
    }

    @Transactional
    public void withdrawUser(Long userId, UserWithdrawRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if ("DELETED".equals(user.getStatus())) {
            throw new IllegalArgumentException("이미 탈퇴한 사용자입니다.");
        }

        if (!user.getPassword().equals(request.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        user.withdraw();
    }
}