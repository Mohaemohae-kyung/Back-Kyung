package kyung.kung_backend.domain.user.entity;

import jakarta.persistence.*;
import kyung.kung_backend.global.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "USERS")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "USERS_SEQ_GENERATOR",
        sequenceName = "USERS_SEQ",
        allocationSize = 1
)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "USERS_SEQ_GENERATOR")
    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @Column(name = "EMAIL", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "PASSWORD", nullable = false, length = 255)
    private String password;

    @Column(name = "NAME", nullable = false, length = 50)
    private String name;

    @Column(name = "NICKNAME", length = 50)
    private String nickname;

    @Column(name = "PHONE", length = 30)
    private String phone;

    @Column(name = "ROLE", nullable = false, length = 20)
    private String role;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;

    @Column(name = "PROFILE_IMAGE_URL", length = 500)
    private String profileImageUrl;

    @Column(name = "LAST_LOGIN_AT")
    private LocalDateTime lastLoginAt;

    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    public void updateProfile(String name, String phone, String nickname, String profileImageUrl) {
        if (name != null) this.name = name;
        if (phone != null) this.phone = phone;
        if (nickname != null) this.nickname = nickname;
        if (profileImageUrl != null) this.profileImageUrl = profileImageUrl;
    }

    public void withdraw() {
        this.status = "DELETED";
        this.deletedAt = LocalDateTime.now();
    }
}