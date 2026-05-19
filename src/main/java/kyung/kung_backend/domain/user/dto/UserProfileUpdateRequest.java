package kyung.kung_backend.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserProfileUpdateRequest {
    private String name;
    private String phone;
    private String nickname;
    private Long profileImageFileId;
}