package kyung.kung_backend.domain.admin.service;

import kyung.kung_backend.domain.admin.dto.AdminUserDetailResponse;
import kyung.kung_backend.domain.admin.dto.AdminUserListResponse;
import kyung.kung_backend.domain.admin.dto.AdminUserSuspendRequest;
import kyung.kung_backend.domain.admin.entity.AdminAction;
import kyung.kung_backend.domain.admin.repository.AdminActionRepository;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserRepository userRepository;
    private final AdminActionRepository adminActionRepository;

    public List<AdminUserListResponse> getUsers() {
        return userRepository.findAll().stream()
                .map(user -> AdminUserListResponse.builder()
                        .userId(user.getUserId())
                        .email(user.getEmail())
                        .name(user.getName())
                        .nickname(user.getNickname())
                        .phone(user.getPhone())
                        .role(user.getRole())
                        .status(user.getStatus())
                        .createdAt(user.getCreatedAt())
                        .build())
                .toList();
    }

    public AdminUserDetailResponse getUserDetail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        return AdminUserDetailResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .phone(user.getPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .expert("EXPERT".equals(user.getRole()))
                .build();
    }

    @Transactional
    public void suspendUser(Long adminId, Long userId, AdminUserSuspendRequest request) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("관리자를 찾을 수 없습니다."));

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        if ("SUSPENDED".equals(targetUser.getStatus())) {
            throw new IllegalArgumentException("이미 정지된 회원입니다.");
        }

        targetUser.suspend();

        AdminAction adminAction = AdminAction.create(
                admin,
                "USER",
                targetUser.getUserId(),
                "SUSPEND",
                request.getReason()
        );

        adminActionRepository.save(adminAction);
    }
}