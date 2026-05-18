package kyung.kung_backend.domain.auth.dto;

import kyung.kung_backend.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignupResponse {

    private Long userId;
    private String email;
    private String name;
    private String nickname;
    private String phone;
    private String role;
    private String status;

    public static SignupResponse from(User user) {
        return SignupResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .phone(user.getPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }
}