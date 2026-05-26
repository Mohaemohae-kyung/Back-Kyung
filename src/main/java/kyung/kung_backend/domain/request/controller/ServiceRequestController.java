package kyung.kung_backend.domain.request.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kyung.kung_backend.domain.request.dto.ServiceRequestCreateRequest;
import kyung.kung_backend.domain.request.dto.ServiceRequestResponse;
import kyung.kung_backend.domain.request.dto.ServiceRequestUpdateRequest;
import kyung.kung_backend.domain.request.service.ServiceRequestService;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.response.ApiResponse;
import kyung.kung_backend.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Service Request", description = "견적 요청 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/service-requests")
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;

    // =========================
    // 견적 요청 생성
    // =========================
    @Operation(
            summary = "견적 요청 생성",
            description = "로그인 사용자가 선택한 고수 서비스에 대해 견적 요청을 생성합니다."
    )
    @PostMapping
    public ApiResponse<ServiceRequestResponse> createServiceRequest(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ServiceRequestCreateRequest request
    ) {

        ServiceRequestResponse response =
                serviceRequestService.createServiceRequest(user, request);

        return ApiResponse.onSuccess(
                SuccessCode.CREATED,
                response
        );
    }

    // =========================
    // 내가 보낸 요청 조회
    // =========================
    @Operation(
            summary = "내가 보낸 견적 요청 목록 조회",
            description = "일반 사용자가 본인이 보낸 견적 요청 목록을 조회합니다."
    )
    @GetMapping("/sent")
    public ApiResponse<List<ServiceRequestResponse>> getSentServiceRequests(
            @AuthenticationPrincipal User user
    ) {

        List<ServiceRequestResponse> response =
                serviceRequestService.getMyServiceRequests(user);

        return ApiResponse.onSuccess(
                SuccessCode.OK,
                response
        );
    }

    // =========================
    // 받은 요청 조회
    // =========================
    @Operation(
            summary = "받은 견적 요청 목록 조회",
            description = "로그인한 고수가 본인 서비스로 받은 견적 요청 목록을 조회합니다."
    )
    @GetMapping("/received")
    public ApiResponse<List<ServiceRequestResponse>> getReceivedServiceRequests(
            @AuthenticationPrincipal User user
    ) {

        List<ServiceRequestResponse> response =
                serviceRequestService.getReceivedServiceRequests(user);

        return ApiResponse.onSuccess(
                SuccessCode.OK,
                response
        );
    }

    // =========================
    // 내 요청 조회 (기존 유지)
    // =========================
    @Operation(
            summary = "내 견적 요청 목록 조회",
            description = "로그인 사용자가 작성한 견적 요청 목록을 조회합니다."
    )
    @GetMapping("/me")
    public ApiResponse<List<ServiceRequestResponse>> getMyServiceRequests(
            @AuthenticationPrincipal User user
    ) {

        List<ServiceRequestResponse> response =
                serviceRequestService.getMyServiceRequests(user);

        return ApiResponse.onSuccess(
                SuccessCode.OK,
                response
        );
    }

    // =========================
    // 요청 상세 조회
    // =========================
    @Operation(
            summary = "견적 요청 상세 조회",
            description = "견적 요청 상세 정보를 조회합니다. 작성자 본인 또는 관리자만 조회할 수 있습니다."
    )
    @GetMapping("/{requestId}")
    public ApiResponse<ServiceRequestResponse> getServiceRequestDetail(
            @AuthenticationPrincipal User user,
            @PathVariable Long requestId
    ) {

        ServiceRequestResponse response =
                serviceRequestService.getServiceRequestDetail(user, requestId);

        return ApiResponse.onSuccess(
                SuccessCode.OK,
                response
        );
    }

    // =========================
    // 요청 수정
    // =========================
    @PatchMapping("/{requestId}")
    public ApiResponse<ServiceRequestResponse> updateServiceRequest(
            @AuthenticationPrincipal User user,
            @PathVariable Long requestId,
            @Valid @RequestBody ServiceRequestUpdateRequest request
    ) {

        ServiceRequestResponse response =
                serviceRequestService.updateServiceRequest(
                        user,
                        requestId,
                        request
                );

        return ApiResponse.onSuccess(
                SuccessCode.OK,
                response
        );
    }

    // =========================
    // 요청 취소
    // =========================
    @PatchMapping("/{requestId}/cancel")
    public ApiResponse<ServiceRequestResponse> cancelServiceRequest(
            @AuthenticationPrincipal User user,
            @PathVariable Long requestId
    ) {

        ServiceRequestResponse response =
                serviceRequestService.cancelServiceRequest(
                        user,
                        requestId
                );

        return ApiResponse.onSuccess(
                SuccessCode.OK,
                response
        );
    }

    // =========================
    // 요청 승인
    // =========================
    @PatchMapping("/{requestId}/approve")
    public ApiResponse<ServiceRequestResponse> approveServiceRequest(
            @AuthenticationPrincipal User user,
            @PathVariable Long requestId
    ) {

        ServiceRequestResponse response =
                serviceRequestService.approveServiceRequest(
                        user,
                        requestId
                );

        return ApiResponse.onSuccess(
                SuccessCode.OK,
                response
        );
    }

    // =========================
    // 요청 거절
    // =========================
    @PatchMapping("/{requestId}/reject")
    public ApiResponse<ServiceRequestResponse> rejectServiceRequest(
            @AuthenticationPrincipal User user,
            @PathVariable Long requestId
    ) {

        ServiceRequestResponse response =
                serviceRequestService.rejectServiceRequest(
                        user,
                        requestId
                );

        return ApiResponse.onSuccess(
                SuccessCode.OK,
                response
        );
    }
}