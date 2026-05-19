package kyung.kung_backend.domain.notice.controller;

import jakarta.validation.Valid;
import kyung.kung_backend.domain.notice.dto.NoticePostCreateRequest;
import kyung.kung_backend.domain.notice.dto.NoticePostResponse;
import kyung.kung_backend.domain.notice.service.NoticeService;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.response.ApiResponse;
import kyung.kung_backend.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping("/api/expert-center/posts")
    @PreAuthorize("hasRole('EXPERT') or hasRole('ADMIN')")
    public ApiResponse<Page<NoticePostResponse>> getNoticePosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<NoticePostResponse> response = noticeService.getNoticePosts(pageable);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @GetMapping("/api/expert-center/posts/{postId}")
    @PreAuthorize("hasRole('EXPERT') or hasRole('ADMIN')")
    public ApiResponse<NoticePostResponse> getNoticePost(
            @PathVariable Long postId
    ) {
        NoticePostResponse response = noticeService.getNoticePost(postId);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @PostMapping("/api/admin/expert-center/posts")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<NoticePostResponse> createAdminNotice(
            @AuthenticationPrincipal User admin,
            @Valid @RequestBody NoticePostCreateRequest request
    ) {
        NoticePostResponse response = noticeService.createAdminNotice(admin, request);
        return ApiResponse.onSuccess(SuccessCode.CREATED, response);
    }
}