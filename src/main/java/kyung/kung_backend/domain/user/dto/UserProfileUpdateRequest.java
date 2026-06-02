package kyung.kung_backend.domain.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserProfileUpdateRequest {
    private String name;
    private String phone;
    private String nickname;
    private Long profileImageFileId;

    @Size(max = 500, message = "Detail address must be 500 characters or less.")
    private String detailAddress;
}
