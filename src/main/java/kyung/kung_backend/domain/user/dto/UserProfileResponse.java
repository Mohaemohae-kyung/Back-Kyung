package kyung.kung_backend.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileResponse {

    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String nickname;
    private String role;
    private String profileImageUrl;
    private String residentRegistrationNumberMasked;
    private String detailAddress;

    // 고수 사용자인 경우에만 값 존재
    private Long expertProfileId;

    private boolean hasPaymentPassword;
}
