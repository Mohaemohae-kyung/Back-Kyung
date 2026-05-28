// Coupon.java 내부 수정 (삭제할 필드 및 메서드 제거)
package kyung.kung_backend.domain.coupon.entity;

import jakarta.persistence.*;
import kyung.kung_backend.global.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "COUPONS")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "COUPONS_SEQ_GENERATOR")
    @Column(name = "COUPON_ID", nullable = false)
    private Long couponId;

    @Column(name = "CODE", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    // 고정 할인 금액 컬럼만 남겨둡니다.
    @Column(name = "DISCOUNT_AMOUNT", precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "START_AT", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;

    public static final String STATUS_ACTIVE = "ACTIVE";

    // 만료일(END_AT) 체크 로직이 사라지므로, 시작일과 상태값만으로 활성화 여부를 판단하도록 변경합니다.
    public boolean isActive(LocalDateTime now) {
        return STATUS_ACTIVE.equals(this.status) && !now.isBefore(this.startAt);
    }
}