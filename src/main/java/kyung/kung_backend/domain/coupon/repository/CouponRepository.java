package kyung.kung_backend.domain.coupon.repository;

import kyung.kung_backend.domain.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
}