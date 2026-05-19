package kyung.kung_backend.domain.user.service;

import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(
            summary = "유저 엔티티 조회 및 탈퇴 검증",
            description = "고유 식별자로 유저 도메인을 조회하며, 이미 영구 상태가 DELETED로 처리된 탈퇴 회원인 경우 접근 예외를 발생시킵니다."
    )
    public User getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if ("DELETED".equals(user.getStatus())) {
            throw new IllegalArgumentException("탈퇴한 사용자입니다.");
        }
        return user;
    }

    @Operation(
            summary = "내 프로필 상세 정보 변환 조회",
            description = "현재 인증 처리가 완료된 사용자의 최신 상태를 복사 및 조회하여 프로필 전용 DTO 응답 객체로 형변환합니다."
    )
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

    @Operation(
            summary = "개인 신원 정보 수정 및 갱신",
            description = "전달받은 신원 변경 요청 필드들을 엔티티 도메인에 위임 반영하고 수정이 완료된 프로필 데이터를 재출력합니다."
    )
    @Transactional
    public UserProfileResponse updateMyProfile(User currentUser, UserProfileUpdateRequest request) {
        User user = getUser(currentUser.getUserId());
        String newProfileImageUrl = user.getProfileImageUrl();

        // 추후 FileUpload 도메인 연동 시 이미지 URL 갱신 로직 추가 필요

        user.updateProfile(request.getName(), request.getPhone(), request.getNickname(), newProfileImageUrl);
        return getMyProfile(user);
    }

    @Operation(
            summary = "계정 비밀번호 검증 및 논리 탈퇴 처리",
            description = "사용자가 입력한 기존 암호의 일치 여부를 매칭 검증한 후 통과 시 회원 상태를 논리 삭제 처리합니다."
    )
    @Transactional
    public void deleteMyAccount(User currentUser, UserWithdrawRequest request) {
        User user = getUser(currentUser.getUserId());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        user.delete();
    }
}