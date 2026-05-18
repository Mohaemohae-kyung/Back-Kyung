package kyung.kung_backend.domain.community.controller;

import jakarta.validation.Valid;
import kyung.kung_backend.domain.community.dto.PostCreateRequest;
import kyung.kung_backend.domain.community.dto.PostResponse;
import kyung.kung_backend.domain.community.service.CommunityPostService;
import kyung.kung_backend.global.response.ApiResponse;
import kyung.kung_backend.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class CommunityPostController {

    private final CommunityPostService communityPostService;

    @PostMapping
    public ApiResponse<PostResponse> createPost(@Valid @RequestBody PostCreateRequest request) {
        Long currentUserId = 1L;

        PostResponse response = communityPostService.createPost(currentUserId, request);
        return ApiResponse.onSuccess(SuccessCode.CREATED, response);
    }
}