package kyung.kung_backend.domain.payment.repository;

import kyung.kung_backend.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Payment 엔티티에 대한 DB 접근 Repository입니다.
 *
 * 기본 역할:
 * - JpaRepository를 상속해 save, findById, delete 같은 기본 CRUD 기능을 제공합니다.
 *
 * prepare API에서 사용하는 곳:
 * - PaymentService.generateUniqueOrderId()가 서버 주문번호 중복 여부를 확인할 때 사용합니다.
 *
 * 이후 confirm API에서 사용하는 곳:
 * - PG 승인 요청으로 전달받은 orderId를 기준으로 결제 준비 데이터를 다시 찾을 때 사용합니다.
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * ORDER_ID가 이미 존재하는지 확인합니다.
     *
     * 호출 위치:
     * - PaymentService.generateUniqueOrderId()
     *
     * 사용 이유:
     * - ORDER_ID는 PG 결제 승인 단계에서 서버 주문을 식별하는 값입니다.
     * - DB unique 제약으로도 중복은 막히지만, 저장 전에 한 번 확인해 충돌 가능성을 낮춥니다.
     */
    boolean existsByOrderId(String orderId);

    /**
     * ORDER_ID로 결제 데이터를 조회합니다.
     *
     * 현재 prepare 단계에서는 직접 사용하지 않지만,
     * 다음 confirm API에서 paymentKey/orderId/amount 검증을 할 때 사용할 예정입니다.
     */
    Optional<Payment> findByOrderId(String orderId);
}
