package kyung.kung_backend.domain.auth.dto;

import kyung.kung_backend.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private Long userId;
    private String email;
    private String name;
    private String nickname;
    private String role;
    private String accessToken;
    private String refreshToken;

    public static LoginResponse of(User user, String accessToken, String refreshToken) {
        return LoginResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .role(user.getRole())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}