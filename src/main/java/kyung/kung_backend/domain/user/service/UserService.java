package kyung.kung_backend.domain.user.service;

import kyung.kung_backend.domain.expert.entity.ExpertProfile;
import kyung.kung_backend.domain.expert.repository.ExpertProfileRepository;
import kyung.kung_backend.domain.file.entity.FileUpload;
import kyung.kung_backend.domain.file.repository.FileUploadRepository;
import kyung.kung_backend.domain.servicepost.repository.ExpertServiceRepository;
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
    private final ExpertProfileRepository expertProfileRepository;
    private final ExpertServiceRepository expertServiceRepository;
    private final FileUploadRepository fileUploadRepository;

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

        Long expertProfileId =
                expertProfileRepository.findByUser(user)
                        .map(ExpertProfile::getExpertProfileId)
                        .orElse(null);

        return UserProfileResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .role(user.getRole())
                .profileImageUrl(user.getProfileImageUrl())
                .expertProfileId(expertProfileId)
                .hasPaymentPassword(user.getPaymentPasswordHash() != null)
                .build();
    }

    @Transactional
    public void updateFcmToken(User currentUser, String token) {
        User user = getUser(currentUser.getUserId());
        user.updateFcmToken(token);
    }

    @Transactional
    public UserProfileResponse updateMyProfile(User currentUser, UserProfileUpdateRequest request) {
        User user = getUser(currentUser.getUserId());
        String newProfileImageUrl = user.getProfileImageUrl();

        if (request.getProfileImageFileId() != null) {
            FileUpload file = fileUploadRepository.findById(request.getProfileImageFileId())
                    .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다."));
            if (!file.getUploader().getUserId().equals(user.getUserId())) {
                throw new IllegalArgumentException("본인이 업로드한 파일만 사용할 수 있습니다.");
            }
            newProfileImageUrl = file.getFileUrl();
            file.updateTarget("USER_PROFILE", user.getUserId());
        }

        user.updateProfile(request.getName(), request.getPhone(), request.getNickname(), newProfileImageUrl);
        return getMyProfile(user);
    }

    @Transactional
    public void deleteMyAccount(User currentUser, UserWithdrawRequest request) {
        User user = getUser(currentUser.getUserId());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        expertProfileRepository.findByUser(user).ifPresent(profile -> {
            expertServiceRepository.deleteByExpertProfile(profile);
            profile.delete();
        });

        user.delete();
    }
}