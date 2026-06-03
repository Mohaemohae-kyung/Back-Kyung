package kyung.kung_backend.domain.expert.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kyung.kung_backend.domain.expert.dto.ExpertDetailResponse;
import kyung.kung_backend.domain.expert.dto.ExpertProfileCreateRequest;
import kyung.kung_backend.domain.expert.dto.ExpertProfileImageUploadResponse;
import kyung.kung_backend.domain.expert.dto.ExpertProfileUpdateRequest;
import kyung.kung_backend.domain.expert.dto.ExpertSearchResponse;
import kyung.kung_backend.domain.expert.service.ExpertProfileImageService;
import kyung.kung_backend.domain.expert.service.ExpertService;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.response.ApiResponse;
import kyung.kung_backend.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Expert", description = "Expert profile and search APIs")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/experts")
public class ExpertController {

    private final ExpertService expertService;
    private final ExpertProfileImageService expertProfileImageService;

    @Operation(
            summary = "Create expert profile",
            description = "Creates the logged-in user's expert profile."
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
            summary = "Update expert profile",
            description = "Updates the logged-in expert's profile."
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
            summary = "Upload expert profile image",
            description = "Stores only expert profile images in the EC2 local upload directory."
    )
    @PostMapping(
            value = "/profile/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<ExpertProfileImageUploadResponse> uploadProfileImage(
            @AuthenticationPrincipal User user,
            @RequestPart("file") MultipartFile file
    ) {
        ExpertProfileImageUploadResponse response =
                expertProfileImageService.uploadProfileImage(user, file);

        return ApiResponse.onSuccess(SuccessCode.CREATED, response);
    }

    @Operation(
            summary = "Search experts",
            description = "Searches active experts by category, location, and keyword."
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
            summary = "Get expert detail",
            description = "Returns a selected expert profile detail."
    )
    @GetMapping("/{expertProfileId}")
    public ResponseEntity<ApiResponse<ExpertDetailResponse>> getExpertDetail(
            @PathVariable Long expertProfileId
    ) {
        ExpertDetailResponse response =
                expertService.getExpertDetail(expertProfileId);

        return ResponseEntity.ok(
                ApiResponse.onSuccess(response)
        );
    }
}
