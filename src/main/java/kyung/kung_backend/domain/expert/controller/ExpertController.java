package kyung.kung_backend.domain.expert.controller;

import kyung.kung_backend.domain.expert.dto.ExpertProfileCreateRequest;
import kyung.kung_backend.domain.expert.service.ExpertService;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.response.ApiResponse;
import kyung.kung_backend.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
}