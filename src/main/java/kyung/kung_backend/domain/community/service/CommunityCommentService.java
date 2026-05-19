package kyung.kung_backend.domain.community.service;

import io.swagger.v3.oas.annotations.Operation;
import kyung.kung_backend.domain.community.dto.CommentRequest;
import kyung.kung_backend.domain.community.dto.CommentResponse;
import kyung.kung_backend.domain.community.entity.CommunityComment;
import kyung.kung_backend.domain.community.entity.CommunityPost;
import kyung.kung_backend.domain.community.repository.CommunityCommentRepository;
import kyung.kung_backend.domain.community.repository.CommunityPostRepository;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityCommentService {

    private final CommunityCommentRepository communityCommentRepository;
    private final CommunityPostRepository communityPostRepository;
    private final UserRepository userRepository;

    @Operation(
            summary = "활성화된 댓글 목록 전체 조회",
            description = "특정 게시글에 종속되어 있는 댓글 중 ACTIVE 상태를 가진 목록을 조회하여 DTO 리스트로 반ذن합니다."
    )
    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long postId) {
        return communityCommentRepository.findByCommunityPostCommunityPostIdAndStatus(postId, "ACTIVE")
                .stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());
    }

    @Operation(
            summary = "댓글 작성",
            description = "사용자 정보와 대상 게시글 존재 여부를 확인하고 새로운 댓글을 생성하여 저장합니다."
    )
    @Transactional
    public CommentResponse createComment(Long userId, Long postId, CommentRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        CommunityComment comment = CommunityComment.builder()
                .communityPost(post)
                .user(user)
                .content(request.getContent())
                .status("ACTIVE")
                .build();

        CommunityComment savedComment = communityCommentRepository.save(comment);
        return CommentResponse.from(savedComment);
    }

    @Operation(
            summary = "댓글 수정",
            description = "댓글 작성자 본인 여부를 검증한 후, 댓글의 텍스트 본문(content) 내용을 업데이트합니다."
    )
    @Transactional
    public CommentResponse updateComment(Long userId, Long commentId, CommentRequest request) {
        CommunityComment comment = communityCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        if (!comment.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        comment.updateContent(request.getContent());
        return CommentResponse.from(comment);
    }

    @Operation(
            summary = "댓글 소프트 삭제",
            description = "댓글 작성자 본인 여부를 검증한 후, 해당 댓글의 노출 상태를 비활성화 처리합니다."
    )
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        CommunityComment comment = communityCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        if (!comment.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        comment.softDelete();
    }
}