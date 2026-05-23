package kyung.kung_backend.domain.community.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import kyung.kung_backend.domain.community.dto.CommentRequest;
import kyung.kung_backend.domain.community.dto.CommentResponse;
import kyung.kung_backend.domain.community.dto.PostCreateRequest;
import kyung.kung_backend.domain.community.dto.PostResponse;
import kyung.kung_backend.domain.community.dto.PostUpdateRequest;
import kyung.kung_backend.domain.community.service.CommunityCommentService;
import kyung.kung_backend.domain.community.service.CommunityPostService;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.response.ApiResponse;
import kyung.kung_backend.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Community", description = "커뮤니티 게시글 및 댓글 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/community")
public class CommunityPostController {

    private final CommunityPostService communityPostService;
    private final CommunityCommentService communityCommentService;

    @Operation(
            summary = "게시글 목록 조회",
            description = "커뮤니티 게시글 목록을 페이지 단위로 조회합니다."
    )
    @GetMapping("/posts")
    public ApiResponse<Page<PostResponse>> getPosts(
            Pageable pageable,
            @RequestParam(value = "sortColumn", required = false) String sortColumn,
            @RequestParam(value = "sortDirection", required = false, defaultValue = "DESC") String sortDirection) {
        Page<PostResponse> response = communityPostService.getPosts(pageable, sortColumn, sortDirection);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @Operation(
            summary = "게시글 상세 조회",
            description = "선택한 게시글의 상세 정보를 조회합니다."
    )
    @GetMapping("/posts/{postId}")
    public ApiResponse<PostResponse> getPost(@PathVariable Long postId) {
        PostResponse response = communityPostService.getPost(postId);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @Operation(
            summary = "게시글 작성",
            description = "로그인한 사용자가 새로운 게시글을 작성합니다."
    )
    @PostMapping("/posts")
    public ApiResponse<PostResponse> createPost(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody PostCreateRequest request) {
        Long currentUserId = user.getUserId();
        PostResponse response = communityPostService.createPost(currentUserId, request);
        return ApiResponse.onSuccess(SuccessCode.CREATED, response);
    }

    @Operation(
            summary = "게시글 수정",
            description = "작성자 본인이 게시글 내용을 수정합니다."
    )
    @PatchMapping("/posts/{postId}")
    public ApiResponse<PostResponse> updatePost(
            @AuthenticationPrincipal User user,
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest request) {
        Long currentUserId = user.getUserId();
        PostResponse response = communityPostService.updatePost(currentUserId, postId, request);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @Operation(
            summary = "게시글 삭제",
            description = "작성자 본인 또는 관리자가 게시글을 삭제합니다."
    )
    @DeleteMapping("/posts/{postId}")
    public ApiResponse<Void> deletePost(
            @AuthenticationPrincipal User user,
            @PathVariable Long postId) {
        Long currentUserId = user.getUserId();
        communityPostService.deletePost(currentUserId, postId);
        return ApiResponse.onSuccess(SuccessCode.NO_CONTENT);
    }

    @Operation(
            summary = "댓글 목록 조회",
            description = "선택한 게시글의 댓글 목록을 조회합니다."
    )
    @GetMapping("/posts/{postId}/comments")
    public ApiResponse<List<CommentResponse>> getComments(@PathVariable Long postId) {
        List<CommentResponse> response = communityCommentService.getComments(postId);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @Operation(
            summary = "댓글 작성",
            description = "로그인한 사용자가 게시글에 댓글을 작성합니다."
    )
    @PostMapping("/posts/{postId}/comments")
    public ApiResponse<CommentResponse> createComment(
            @AuthenticationPrincipal User user,
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequest request) {
        Long currentUserId = user.getUserId();
        CommentResponse response = communityCommentService.createComment(currentUserId, postId, request);
        return ApiResponse.onSuccess(SuccessCode.CREATED, response);
    }

    @Operation(
            summary = "댓글 수정",
            description = "작성자 본인이 댓글 내용을 수정합니다."
    )
    @PatchMapping("/comments/{commentId}")
    public ApiResponse<CommentResponse> updateComment(
            @AuthenticationPrincipal User user,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest request) {
        Long currentUserId = user.getUserId();
        CommentResponse response = communityCommentService.updateComment(currentUserId, commentId, request);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @Operation(
            summary = "댓글 삭제",
            description = "작성자 본인 또는 관리자가 댓글을 삭제합니다."
    )
    @DeleteMapping("/comments/{commentId}")
    public ApiResponse<Void> deleteComment(
            @AuthenticationPrincipal User user,
            @PathVariable Long commentId) {

        Long currentUserId = user.getUserId();
        communityCommentService.deleteComment(currentUserId, commentId);

        return ApiResponse.onSuccess(SuccessCode.NO_CONTENT);
    }
}