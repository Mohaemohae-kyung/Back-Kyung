package kyung.kung_backend.domain.community.controller;

import jakarta.validation.Valid;
import kyung.kung_backend.domain.community.dto.CommentRequest;
import kyung.kung_backend.domain.community.dto.CommentResponse;
import kyung.kung_backend.domain.community.dto.PostCreateRequest;
import kyung.kung_backend.domain.community.dto.PostResponse;
import kyung.kung_backend.domain.community.dto.PostUpdateRequest;
import kyung.kung_backend.domain.community.service.CommunityCommentService;
import kyung.kung_backend.domain.community.service.CommunityPostService;
import kyung.kung_backend.global.response.ApiResponse;
import kyung.kung_backend.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/community")
public class CommunityPostController {

    private final CommunityPostService communityPostService;
    private final CommunityCommentService communityCommentService;

    @GetMapping("/posts")
    public ApiResponse<Page<PostResponse>> getPosts(Pageable pageable) {
        Page<PostResponse> response = communityPostService.getPosts(pageable);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @GetMapping("/posts/{postId}")
    public ApiResponse<PostResponse> getPost(@PathVariable Long postId) {
        PostResponse response = communityPostService.getPost(postId);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @PostMapping("/posts")
    public ApiResponse<PostResponse> createPost(@Valid @RequestBody PostCreateRequest request) {
        Long currentUserId = 1L;
        PostResponse response = communityPostService.createPost(currentUserId, request);
        return ApiResponse.onSuccess(SuccessCode.CREATED, response);
    }

    @PatchMapping("/posts/{postId}")
    public ApiResponse<PostResponse> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest request) {
        Long currentUserId = 1L;
        PostResponse response = communityPostService.updatePost(currentUserId, postId, request);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @DeleteMapping("/posts/{postId}")
    public ApiResponse<Void> deletePost(@PathVariable Long postId) {
        Long currentUserId = 1L;
        communityPostService.deletePost(currentUserId, postId);
        return ApiResponse.onSuccess(SuccessCode.NO_CONTENT);
    }

    @GetMapping("/posts/{postId}/comments")
    public ApiResponse<List<CommentResponse>> getComments(@PathVariable Long postId) {
        List<CommentResponse> response = communityCommentService.getComments(postId);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @PostMapping("/posts/{postId}/comments")
    public ApiResponse<CommentResponse> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequest request) {
        Long currentUserId = 1L;
        CommentResponse response = communityCommentService.createComment(currentUserId, postId, request);
        return ApiResponse.onSuccess(SuccessCode.CREATED, response);
    }

    @PatchMapping("/comments/{commentId}")
    public ApiResponse<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest request) {
        Long currentUserId = 1L;
        CommentResponse response = communityCommentService.updateComment(currentUserId, commentId, request);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @DeleteMapping("/comments/{commentId}")
    public ApiResponse<Void> deleteComment(@PathVariable Long commentId) {
        Long currentUserId = 1L;
        communityCommentService.deleteComment(currentUserId, commentId);
        return ApiResponse.onSuccess(SuccessCode.NO_CONTENT);
    }
}