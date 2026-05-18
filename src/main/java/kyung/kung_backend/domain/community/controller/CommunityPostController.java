package kyung.kung_backend.domain.community.controller;

import jakarta.validation.Valid;
import kyung.kung_backend.domain.community.dto.PostCreateRequest;
import kyung.kung_backend.domain.community.dto.PostResponse;
import kyung.kung_backend.domain.community.dto.PostUpdateRequest;
import kyung.kung_backend.domain.community.service.CommunityPostService;
import kyung.kung_backend.global.response.ApiResponse;
import kyung.kung_backend.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @GetMapping("/{postId}")
    public ApiResponse<PostResponse> getPost(@PathVariable Long postId) {
        PostResponse response = communityPostService.getPost(postId);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @GetMapping
    public ApiResponse<Page<PostResponse>> getPosts(Pageable pageable) {
        Page<PostResponse> response = communityPostService.getPosts(pageable);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @PutMapping("/{postId}")
    public ApiResponse<PostResponse> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest request) {
        Long currentUserId = 1L;
        PostResponse response = communityPostService.updatePost(currentUserId, postId, request);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @DeleteMapping("/{postId}")
    public ApiResponse<Void> deletePost(@PathVariable Long postId) {
        Long currentUserId = 1L;
        communityPostService.deletePost(currentUserId, postId);
        return ApiResponse.onSuccess(SuccessCode.NO_CONTENT);
    }
}