package kyung.kung_backend.domain.payment.repository;

import kyung.kung_backend.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Payment 엔티티 DB 접근 Repository입니다.
 *
 * 호출 위치:
 * - PaymentService.prepare()에서 결제 준비 데이터를 저장할 때 사용합니다.
 * - 이후 confirm API에서는 orderId 또는 paymentId로 결제 정보를 다시 조회할 때 사용하게 됩니다.
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * 서버 주문번호 중복 여부를 확인합니다.
     *
     * prepare 단계에서 orderId를 생성한 뒤 충돌 가능성을 낮추기 위해 사용합니다.
     */
    boolean existsByOrderId(String orderId);

    /**
     * confirm 단계에서 PG사가 넘겨준 orderId로 결제 준비 정보를 찾기 위한 메서드입니다.
     *
     * 이번 작업은 prepare 스켈레톤이지만, 다음 confirm 작업에서 바로 사용할 수 있도록 미리 둡니다.
     */
    Optional<Payment> findByOrderId(String orderId);
}
