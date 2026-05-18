package kyung.kung_backend.domain.request.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kyung.kung_backend.domain.request.dto.ServiceRequestCreateRequest;
import kyung.kung_backend.domain.request.dto.ServiceRequestResponse;
import kyung.kung_backend.domain.request.dto.ServiceRequestUpdateRequest;
import kyung.kung_backend.domain.request.service.ServiceRequestService;
import kyung.kung_backend.global.response.ApiResponse;
import kyung.kung_backend.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Service Request", description = "견적 요청 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/service-requests")
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;

    @Operation(
            summary = "견적 요청 생성",
            description = "사용자가 선택한 고수 서비스에 대해 견적 요청을 생성합니다."
    )
    @PostMapping
    public ApiResponse<ServiceRequestResponse> createServiceRequest(
            // TODO: JWT 구현 후 SecurityContext에서 로그인 사용자 ID를 조회하도록 변경
            @RequestParam Long userId,
            @Valid @RequestBody ServiceRequestCreateRequest request
    ) {
        ServiceRequestResponse response = serviceRequestService.createServiceRequest(userId, request);
        return ApiResponse.onSuccess(SuccessCode.CREATED, response);
    }

    @Operation(
            summary = "내 견적 요청 목록 조회",
            description = "로그인 사용자가 작성한 견적 요청 목록을 조회합니다."
    )
    @GetMapping("/me")
    public ApiResponse<List<ServiceRequestResponse>> getMyServiceRequests(
            // TODO: JWT 구현 후 SecurityContext에서 로그인 사용자 ID를 조회하도록 변경
            @RequestParam Long userId
    ) {
        List<ServiceRequestResponse> response = serviceRequestService.getMyServiceRequests(userId);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @Operation(
            summary = "견적 요청 상세 조회",
            description = "견적 요청 상세 정보를 조회합니다. 현재는 작성자 본인만 조회할 수 있습니다."
    )
    @GetMapping("/{requestId}")
    public ApiResponse<ServiceRequestResponse> getServiceRequestDetail(
            // TODO: JWT 구현 후 SecurityContext에서 로그인 사용자 ID를 조회하도록 변경
            @RequestParam Long userId,
            @PathVariable Long requestId
    ) {
        ServiceRequestResponse response = serviceRequestService.getServiceRequestDetail(userId, requestId);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @Operation(
            summary = "견적 요청 수정",
            description = "작성자 본인이 견적 요청 내용을 수정합니다. 서비스, 카테고리, 지역은 수정하지 않습니다."
    )
    @PatchMapping("/{requestId}")
    public ApiResponse<ServiceRequestResponse> updateServiceRequest(
            // TODO: JWT 구현 후 SecurityContext에서 로그인 사용자 ID를 조회하도록 변경
            @RequestParam Long userId,
            @PathVariable Long requestId,
            @Valid @RequestBody ServiceRequestUpdateRequest request
    ) {
        ServiceRequestResponse response = serviceRequestService.updateServiceRequest(userId, requestId, request);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @Operation(
            summary = "견적 요청 취소",
            description = "작성자 본인이 견적 요청을 취소합니다. 실제 삭제가 아니라 상태를 CANCELLED로 변경합니다."
    )
    @PatchMapping("/{requestId}/cancel")
    public ApiResponse<ServiceRequestResponse> cancelServiceRequest(
            // TODO: JWT 구현 후 SecurityContext에서 로그인 사용자 ID를 조회하도록 변경
            @RequestParam Long userId,
            @PathVariable Long requestId
    ) {
        ServiceRequestResponse response = serviceRequestService.cancelServiceRequest(userId, requestId);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }
}