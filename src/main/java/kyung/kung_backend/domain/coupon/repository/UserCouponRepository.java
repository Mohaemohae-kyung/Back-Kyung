package kyung.kung_backend.domain.coupon.repository;

import kyung.kung_backend.domain.coupon.entity.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

    Optional<UserCoupon> findByUserCouponIdAndUserUserId(
            Long userCouponId,
            Long userId
    );

    List<UserCoupon> findAllByUserUserId(Long userId);
}