package kyung.kung_backend.domain.admin.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminUserDetailResponse {

    private Long userId;
    private String email;
    private String name;
    private String nickname;
    private String phone;
    private String role;
    private String status;
    private LocalDateTime createdAt;
    private Boolean expert;
}
