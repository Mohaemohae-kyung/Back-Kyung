package kyung.kung_backend.domain.match.repository;

import kyung.kung_backend.domain.match.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Match 엔티티 DB 접근 Repository입니다.
 *
 * 호출 위치:
 * - PaymentService.prepare()에서 matchId만 들어온 경우 결제 대상 매칭을 조회할 때 사용합니다.
 * - bookingId와 matchId가 함께 들어온 경우 예약의 매칭 ID와 요청 matchId가 같은지 검증할 때도 사용 흐름에 포함됩니다.
 */
public interface MatchRepository extends JpaRepository<Match, Long> {
}
