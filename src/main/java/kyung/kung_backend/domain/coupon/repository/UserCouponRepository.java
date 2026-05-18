package kyung.kung_backend.domain.coupon.repository;

import kyung.kung_backend.domain.coupon.entity.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {
}