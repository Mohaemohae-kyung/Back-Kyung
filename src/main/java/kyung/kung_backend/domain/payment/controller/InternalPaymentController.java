package kyung.kung_backend.domain.payment.controller;

import kyung.kung_backend.domain.payment.dto.InternalSuccessRequest;
import kyung.kung_backend.domain.payment.dto.InternalTransactionRequest;
import kyung.kung_backend.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal")
public class InternalPaymentController {

    private final PaymentService paymentService;

    // 모의 실습을 위한 하드코딩 메모리 DB (실제로는 TransactionRepository에서 가져와야 함)
    // 빠른 E2E 실습 구현을 위해 Controller 메모리에 임시 캐싱
    private final Map<String, BigDecimal> transactionAmountDb = new HashMap<>();

    @GetMapping("/items/{itemId}")
    public ResponseEntity<Map<String, Object>> getItemPrice(@PathVariable Long itemId) {
        // 모의 해킹 실습을 위해 모든 아이템의 원가를 100,000원으로 고정 응답
        Map<String, Object> response = new HashMap<>();
        response.put("price", 100000);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transactions")
    public ResponseEntity<String> createTransaction(@RequestBody InternalTransactionRequest request) {
        // E2E 암호서버(Node.js)가 취약한 로직으로 계산한 최종 금액(예: 100원)을 저장 요청함.
        // 이를 메모리 맵이나 실제 DB에 저장
        transactionAmountDb.put(request.getOrderId(), request.getFinalAmount());
        return ResponseEntity.ok("Transaction saved successfully");
    }

    @GetMapping("/transactions/{orderId}")
    public ResponseEntity<Map<String, Object>> getTransaction(@PathVariable String orderId) {
        // Node.js 서버가 2차 컨펌(Confirm) 대조를 위해 저장된 금액을 물어봄
        BigDecimal amount = transactionAmountDb.getOrDefault(orderId, BigDecimal.ZERO);
        Map<String, Object> response = new HashMap<>();
        response.put("finalAmount", amount);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/payments/success")
    public ResponseEntity<String> markPaymentSuccess(@RequestBody InternalSuccessRequest request) {
        // 토스 결제 승인 후 호출됨
        // 원래라면 Payment, Transaction 상태를 PAID로 바꾸고 User의 쿠폰을 회수해야 함.
        // 현재는 실습용 mock 응답
        return ResponseEntity.ok("Payment confirmed and coupon flag updated successfully");
    }
}
