package kyung.kung_backend.domain.servicepost.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import kyung.kung_backend.domain.servicepost.dto.ExpertServiceCreateRequest;
import kyung.kung_backend.domain.servicepost.dto.ExpertServiceResponse;
import kyung.kung_backend.domain.servicepost.service.ExpertServicePostService;

import kyung.kung_backend.domain.user.entity.User;

import kyung.kung_backend.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Expert Service",
        description = "고수 서비스 관련 API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/expert-services")
public class ExpertServiceController {

    private final ExpertServicePostService
            expertServicePostService;

    @Operation(summary = "고수 서비스 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<ExpertServiceResponse>>
    createExpertService(

            // 수정됨
            // expression 제거
            @AuthenticationPrincipal
            User user,

            @RequestBody
            ExpertServiceCreateRequest request
    ) {

        ExpertServiceResponse response =

                expertServicePostService
                        .createExpertService(
                                user,
                                request
                        );

        return ResponseEntity.ok(

                ApiResponse.onSuccess(response)
        );
    }

    @Operation(summary = "고수 서비스 상세 조회")
    @GetMapping("/{serviceId}")
    public ResponseEntity<ApiResponse<ExpertServiceResponse>>
    getExpertServiceDetail(

            @PathVariable
            Long serviceId
    ) {

        ExpertServiceResponse response =

                expertServicePostService
                        .getExpertServiceDetail(
                                serviceId
                        );

        return ResponseEntity.ok(

                ApiResponse.onSuccess(response)
        );
    }
}