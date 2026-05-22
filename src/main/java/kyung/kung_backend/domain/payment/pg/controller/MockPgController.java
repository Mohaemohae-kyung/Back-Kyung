package kyung.kung_backend.domain.payment.pg.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kyung.kung_backend.domain.payment.pg.dto.MockPgApproveRequest;
import kyung.kung_backend.domain.payment.pg.dto.MockPgApproveResponse;
import kyung.kung_backend.domain.payment.pg.service.MockPgService;
import kyung.kung_backend.global.response.ApiResponse;
import kyung.kung_backend.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mock-pg")
@Tag(name = "Mock PG API", description = "실제 PG 연동 전 결제 승인 흐름을 테스트하기 위한 간단한 Mock PG API")
public class MockPgController {

    private final MockPgService mockPgService;

    @Operation(
            summary = "Mock PG 결제 승인",
            description = "payments/prepare 응답의 orderId와 finalAmount를 받아 PG사가 결제를 승인한 것처럼 MOCK_PG_PAYMENTS에 기록합니다. " +
                    "응답으로 받은 paymentKey를 payments/confirm에 전달해야 최종 결제가 완료됩니다."
    )
    @PostMapping("/approve")
    public ResponseEntity<ApiResponse<MockPgApproveResponse>> approve(
            @Valid @RequestBody MockPgApproveRequest request
    ) {
        MockPgApproveResponse response = mockPgService.approve(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.onSuccess(SuccessCode.CREATED, response));
    }
}
