package kyung.kung_backend.domain.expert.controller;

import kyung.kung_backend.domain.expert.dto.ExpertProfileCreateRequest;
import kyung.kung_backend.domain.expert.dto.ExpertProfileUpdateRequest;
import kyung.kung_backend.domain.expert.dto.ExpertSearchResponse;
import kyung.kung_backend.domain.expert.dto.ExpertDetailResponse;
import kyung.kung_backend.domain.expert.dto.ExpertActivitySelectionRequest;
import kyung.kung_backend.domain.expert.service.ExpertService;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.response.ApiResponse;
import kyung.kung_backend.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/experts")
public class ExpertController {

    private final ExpertService expertService;

    @PostMapping("/profile")
    public ApiResponse<Void> createProfile(
            @AuthenticationPrincipal User user,
            @RequestBody ExpertProfileCreateRequest request
    ) {
        expertService.createProfile(user, request);
        return ApiResponse.onSuccess(SuccessCode.CREATED);
    }

    @PatchMapping("/profile")
    public ApiResponse<Void> updateProfile(
            @AuthenticationPrincipal User user,
            @RequestBody ExpertProfileUpdateRequest request
    ) {
        expertService.updateProfile(user, request);
        return ApiResponse.onSuccess(SuccessCode.OK);
    }

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

    @GetMapping("/{expertId}")
    public ApiResponse<ExpertDetailResponse> getExpertDetail(
            @PathVariable Long expertId
    ) {
        ExpertDetailResponse response = expertService.getExpertDetail(expertId);
        return ApiResponse.onSuccess(response);
    }

    @PostMapping("/me/activity-selection")
    public ApiResponse<Void> selectActivity(
            @AuthenticationPrincipal User user,
            @RequestBody ExpertActivitySelectionRequest request
    ) {
        expertService.selectActivity(user, request);
        return ApiResponse.onSuccess(SuccessCode.CREATED);
    }
}