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

    @Column(name = "LAST_LOGIN_AT")
    private LocalDateTime lastLoginAt;

    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    @Column(name = "REFRESH_TOKEN", length = 500)
    private String refreshToken;

    @Column(name = "PROFILE_IMAGE_URL", length = 500)
    private String profileImageUrl;

    @Column(name = "WELCOME_COUPON_AVAILABLE", length = 1)
    private String welcomeCouponAvailable = "Y";

    public static User createUser(String email, String encodedPassword, String name, String nickname, String phone) {
        User user = new User();
        user.email = email;
        user.password = encodedPassword;
        user.name = name;
        user.nickname = nickname;
        user.phone = phone;
        user.role = "USER";
        user.status = "ACTIVE";
        user.welcomeCouponAvailable = "Y"; // 회원가입 시 웰컴 쿠폰 수령 가능 상태로 초기화
        return user;
    }

    public void delete() {
        this.status = "DELETED";
        this.deletedAt = LocalDateTime.now();
    }

    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void clearRefreshToken() {
        this.refreshToken = null;
    }

    public void updateProfile(String name, String phone, String nickname, String profileImageUrl) {
        if (name != null) this.name = name;
        if (phone != null) this.phone = phone;
        if (nickname != null) this.nickname = nickname;
        if (profileImageUrl != null) this.profileImageUrl = profileImageUrl;
    }

    public void suspend() {
        this.status = "SUSPENDED";
    }

    public void becomeExpert() {
        this.role = "EXPERT";
    }

    public void updatePassword(String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isBlank()) {
            throw new IllegalArgumentException("변경할 비밀번호는 공백일 수 없습니다.");
        }
        this.password = encodedPassword;
    }

    public boolean isWelcomeCouponAvailable() {
        return "Y".equals(this.welcomeCouponAvailable);
    }

    public void disableWelcomeCoupon() {
        this.welcomeCouponAvailable = "N";
    }
}