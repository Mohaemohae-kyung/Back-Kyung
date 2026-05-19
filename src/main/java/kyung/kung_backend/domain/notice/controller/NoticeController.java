package kyung.kung_backend.domain.notice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(
        name = "Notice",
        description = "고수 권한 및 관리자 권한으로 가동되는 전용 공지사항 제어 API"
)
@RestController
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(
            summary = "고수센터 게시글 목록 조회",
            description = "고수(EXPERT) 또는 관리자(ADMIN) 권한 소유자가 전용 게시글 전체 목록을 페이징 조회합니다."
    )
    @GetMapping("/api/expert-center/posts")
    @PreAuthorize("hasRole('EXPERT') or hasRole('ADMIN')")
    public ApiResponse<Page<NoticePostResponse>> getNoticePosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<NoticePostResponse> response = noticeService.getNoticePosts(pageable);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @Operation(
            summary = "고수센터 게시글 상세 조회",
            description = "게시글 식별 번호(postId)를 사용하여 해당 공지 글의 세부 사양을 조회하며 조회수가 1 증가합니다."
    )
    @GetMapping("/api/expert-center/posts/{postId}")
    @PreAuthorize("hasRole('EXPERT') or hasRole('ADMIN')")
    public ApiResponse<NoticePostResponse> getNoticePost(
            @PathVariable Long postId
    ) {
        NoticePostResponse response = noticeService.getNoticePost(postId);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @Operation(
            summary = "고수센터 공지 등록 (관리자)",
            description = "관리자(ADMIN) 권한 소유자가 고수센터용 신규 공지사항 게시글을 생성하며, 첨부파일 식별자 매핑을 수행합니다."
    )
    @PostMapping("/api/admin/expert-center/posts")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<NoticePostResponse> createAdminNotice(
            @AuthenticationPrincipal User admin,
            @Valid @RequestBody NoticePostCreateRequest request
    ) {
        NoticePostResponse response = noticeService.createAdminNotice(admin, request);
        return ApiResponse.onSuccess(SuccessCode.CREATED, response);
    }

    @Operation(
            summary = "고수센터 공지 수정 (관리자)",
            description = "관리자(ADMIN) 권한 소유자가 기존 고수센터 공지사항의 제목과 본문을 수정하며, 새로운 첨부파일 매핑을 갱신합니다."
    )
    @PatchMapping("/api/admin/expert-center/posts/{postId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<NoticePostResponse> updateAdminNotice(
            @PathVariable Long postId,
            @RequestBody NoticePostCreateRequest request
    ) {
        NoticePostResponse response = noticeService.updateAdminNotice(postId, request);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @Operation(
            summary = "고수센터 공지 삭제 (관리자)",
            description = "관리자(ADMIN) 권한 소유자가 고수센터 게시글 식별 번호를 기반으로 상태를 DELETED로 변경하여 논리 삭제합니다."
    )
    @DeleteMapping("/api/admin/expert-center/posts/{postId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteAdminNotice(
            @PathVariable Long postId
    ) {
        noticeService.deleteAdminNotice(postId);
        return ApiResponse.onSuccess(SuccessCode.OK, null);
    }
}