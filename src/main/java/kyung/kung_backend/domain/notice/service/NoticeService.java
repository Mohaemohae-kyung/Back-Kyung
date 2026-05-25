package kyung.kung_backend.domain.notice.service;

import kyung.kung_backend.domain.notice.dto.NoticePostCreateRequest;
import kyung.kung_backend.domain.notice.dto.NoticePostResponse;
import kyung.kung_backend.domain.file.entity.FileUpload;
import kyung.kung_backend.domain.file.repository.FileUploadRepository;
import kyung.kung_backend.domain.notice.entity.Notice;
import kyung.kung_backend.domain.notice.repository.NoticeRepository;
import kyung.kung_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final FileUploadRepository fileUploadRepository;

    public Page<NoticePostResponse> getNoticePosts(
            Pageable pageable,
            String sortColumn,
            String sortDirection
    ) {

        if (sortColumn != null && sortDirection != null) {

            Sort sort = Sort.by(
                    Sort.Direction.fromString(sortDirection),
                    sortColumn
            );

            pageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    sort
            );
        }

        return noticeRepository
                .findByNoticeTypeAndStatus(
                        "EXPERT_NOTICE",
                        "ACTIVE",
                        pageable
                )
                .map(NoticePostResponse::from);
    }

    @Transactional
    public NoticePostResponse getNoticePost(Long postId) {
        Notice notice = noticeRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!"EXPERT_NOTICE".equals(notice.getNoticeType()) || "DELETED".equals(notice.getStatus())) {
            throw new IllegalArgumentException("유효하지 않은 게시글입니다.");
        }

        notice.incrementViewCount();
        return NoticePostResponse.from(notice);
    }

    @Transactional
    public NoticePostResponse createAdminNotice(User admin, NoticePostCreateRequest request) {
        Notice notice = Notice.createNotice(
                admin,
                "EXPERT_NOTICE",
                request.getTitle(),
                request.getContent()
        );

        Notice savedNotice = noticeRepository.save(notice);

        if (request.getAttachmentFileIds() != null && !request.getAttachmentFileIds().isEmpty()) {
            List<FileUpload> files = fileUploadRepository.findAllById(request.getAttachmentFileIds());
            for (FileUpload file : files) {
                file.updateTarget("EXPERT_NOTICE", savedNotice.getNoticeId());
            }
        }

        return NoticePostResponse.from(savedNotice);
    }

    @Transactional
    public NoticePostResponse updateAdminNotice(Long postId, NoticePostCreateRequest request) {
        Notice notice = noticeRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!"EXPERT_NOTICE".equals(notice.getNoticeType()) || "DELETED".equals(notice.getStatus())) {
            throw new IllegalArgumentException("수정할 수 없는 게시글입니다.");
        }

        notice.updateNotice(request.getTitle(), request.getContent());

        if (request.getAttachmentFileIds() != null && !request.getAttachmentFileIds().isEmpty()) {
            List<FileUpload> files = fileUploadRepository.findAllById(request.getAttachmentFileIds());
            for (FileUpload file : files) {
                file.updateTarget("EXPERT_NOTICE", notice.getNoticeId());
            }
        }

        return NoticePostResponse.from(notice);
    }

    @Transactional
    public void deleteAdminNotice(Long postId) {
        Notice notice = noticeRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!"EXPERT_NOTICE".equals(notice.getNoticeType()) || "DELETED".equals(notice.getStatus())) {
            throw new IllegalArgumentException("이미 삭제되었거나 처리할 수 없는 게시글입니다.");
        }

        notice.updateStatus("DELETED");
    }
}