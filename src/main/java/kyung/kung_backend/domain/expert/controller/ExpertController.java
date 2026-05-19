package kyung.kung_backend.domain.expert.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import kyung.kung_backend.domain.expert.dto.ExpertProfileCreateRequest;
import kyung.kung_backend.domain.expert.dto.ExpertProfileUpdateRequest;
import kyung.kung_backend.domain.expert.dto.ExpertSearchResponse;
import kyung.kung_backend.domain.expert.dto.ExpertDetailResponse;
import kyung.kung_backend.domain.expert.service.ExpertService;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.response.ApiResponse;
import kyung.kung_backend.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Expert", description = "고수 프로필 및 검색 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/experts")
public class ExpertController {

    private final ExpertService expertService;

    @Operation(
            summary = "고수 프로필 등록",
            description = "로그인 사용자가 고수로 활동하기 위한 기본 프로필을 등록합니다."
    )
    @PostMapping("/profile")
    public ApiResponse<Void> createProfile(
            @AuthenticationPrincipal User user,
            @RequestBody ExpertProfileCreateRequest request
    ) {
        expertService.createProfile(user, request);
        return ApiResponse.onSuccess(SuccessCode.CREATED);
    }

    @Operation(
            summary = "고수 프로필 수정",
            description = "로그인한 고수가 본인의 프로필 기본 정보를 수정합니다."
    )
    @PatchMapping("/profile")
    public ApiResponse<Void> updateProfile(
            @AuthenticationPrincipal User user,
            @RequestBody ExpertProfileUpdateRequest request
    ) {
        expertService.updateProfile(user, request);
        return ApiResponse.onSuccess(SuccessCode.OK);
    }

    @Operation(
            summary = "고수 검색",
            description = "카테고리, 지역, 키워드 조건으로 활동 중인 고수 목록을 조회합니다."
    )
    @GetMapping("/search")
    public ApiResponse<List<ExpertSearchResponse>> searchExperts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) String keyword
    ) {
        List<ExpertSearchResponse> response =
                expertService.searchExperts(categoryId, locationId, keyword);

        return ApiResponse.onSuccess(response);
    }

    @Operation(
            summary = "고수 상세 조회",
            description = "선택한 고수의 프로필 상세 정보를 조회합니다."
    )
    @GetMapping("/{expertId}")
    public ApiResponse<ExpertDetailResponse> getExpertDetail(
            @PathVariable Long expertId
    ) {
        ExpertDetailResponse response = expertService.getExpertDetail(expertId);
        return ApiResponse.onSuccess(response);
    }
}