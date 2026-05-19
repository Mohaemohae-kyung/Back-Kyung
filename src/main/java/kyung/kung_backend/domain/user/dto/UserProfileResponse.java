package kyung.kung_backend.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileResponse {
    private String name;
    private String email;
    private String phone;
    private String nickname;
    private String role;
    private String profileImageUrl;
}