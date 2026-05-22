package kyung.kung_backend.domain.payment.pg.service;

import kyung.kung_backend.domain.payment.pg.dto.MockPgApproveRequest;
import kyung.kung_backend.domain.payment.pg.dto.MockPgApproveResponse;
import kyung.kung_backend.domain.payment.pg.entity.MockPgPayment;
import kyung.kung_backend.domain.payment.pg.repository.MockPgPaymentRepository;
import kyung.kung_backend.global.exception.GeneralException;
import kyung.kung_backend.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MockPgService {

    private final MockPgPaymentRepository mockPgPaymentRepository;

    /*
     * 실제 PG 결제창에서 결제가 성공했다고 가정하고 승인 기록을 저장합니다.
     * 이미 같은 orderId로 승인된 기록이 있다면 같은 주문을 중복 승인하지 않도록 막습니다.
     */
    @Transactional
    public MockPgApproveResponse approve(MockPgApproveRequest request) {
        if (mockPgPaymentRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw GeneralException.of(ErrorCode.MOCK_PG_DUPLICATE_APPROVAL);
        }

        MockPgPayment mockPgPayment = MockPgPayment.approve(
                request.getOrderId(),
                request.getAmount(),
                request.getPaymentMethod(),
                LocalDateTime.now()
        );

        return MockPgApproveResponse.from(mockPgPaymentRepository.save(mockPgPayment));
    }

    /*
     * payments/confirm에서 호출하는 검증 로직입니다.
     * 클라이언트가 넘긴 paymentKey를 그대로 믿지 않고 Mock PG 승인 테이블에 남은 기록과 대조합니다.
     */
    public void verifyApprovedPayment(
            String orderId,
            String paymentKey,
            BigDecimal amount
    ) {
        MockPgPayment mockPgPayment = mockPgPaymentRepository.findByPaymentKey(paymentKey)
                .orElseThrow(() -> GeneralException.of(ErrorCode.MOCK_PG_NOT_FOUND));

        if (!Objects.equals(mockPgPayment.getOrderId(), orderId)) {
            throw GeneralException.of(ErrorCode.MOCK_PG_INVALID_APPROVAL);
        }

        if (!sameAmount(mockPgPayment.getAmount(), amount)) {
            throw GeneralException.of(ErrorCode.MOCK_PG_INVALID_APPROVAL);
        }

        if (!mockPgPayment.isApproved()) {
            throw GeneralException.of(ErrorCode.MOCK_PG_INVALID_APPROVAL);
        }
    }

    private boolean sameAmount(
            BigDecimal expected,
            BigDecimal actual
    ) {
        return expected != null
                && actual != null
                && expected.compareTo(actual) == 0;
    }
}
