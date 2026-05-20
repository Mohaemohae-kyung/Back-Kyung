package kyung.kung_backend.domain.admin.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminUserSuspendRequest {

    private String reason;
}
