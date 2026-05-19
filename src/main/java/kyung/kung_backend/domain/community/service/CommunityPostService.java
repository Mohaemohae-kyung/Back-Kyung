package kyung.kung_backend.domain.community.service;

import io.swagger.v3.oas.annotations.Operation;
import kyung.kung_backend.domain.category.entity.ServiceCategory;
import kyung.kung_backend.domain.category.repository.ServiceCategoryRepository;
import kyung.kung_backend.domain.community.dto.PostCreateRequest;
import kyung.kung_backend.domain.community.dto.PostResponse;
import kyung.kung_backend.domain.community.dto.PostUpdateRequest;
import kyung.kung_backend.domain.community.entity.CommunityPost;
import kyung.kung_backend.domain.community.repository.CommunityPostRepository;
import kyung.kung_backend.domain.file.entity.FileUpload;
import kyung.kung_backend.domain.file.repository.FileUploadRepository;
import kyung.kung_backend.domain.location.entity.Location;
import kyung.kung_backend.domain.location.repository.LocationRepository;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommunityPostService {

    private final CommunityPostRepository communityPostRepository;
    private final UserRepository userRepository;
    private final ServiceCategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final FileUploadRepository fileUploadRepository;

    @Operation(
            summary = "게시글 작성",
            description = "사용자 식별 번호와 요청 데이터를 기반으로 새로운 커뮤니티 게시글을 등록합니다. 첨부된 파일이 있다면 해당 파일 엔티티의 연동 대상을 업데이트합니다."
    )
    @Transactional
    public PostResponse createPost(Long userId, PostCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        ServiceCategory category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));
        }

        Location location = null;
        if (request.getLocationId() != null) {
            location = locationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new IllegalArgumentException("지역을 찾을 수 없습니다."));
        }

        CommunityPost post = CommunityPost.builder()
                .user(user)
                .category(category)
                .location(location)
                .boardType(request.getBoardType())
                .title(request.getTitle())
                .content(request.getContent())
                .status("ACTIVE")
                .build();

        CommunityPost savedPost = communityPostRepository.save(post);

        if (request.getImageFileIds() != null && !request.getImageFileIds().isEmpty()) {
            List<FileUpload> files = fileUploadRepository.findAllById(request.getImageFileIds());
            for (FileUpload file : files) {
                file.updateTarget("COMMUNITY_POST", savedPost.getCommunityPostId());
            }
        }

        return PostResponse.from(savedPost);
    }

    @Operation(
            summary = "게시글 조회 및 조회수 증가",
            description = "특정 게시글 고유 식별자로 데이터를 조회하고 조회수를 1 증가시켜 반환합니다. 삭제된 게시글은 조회할 수 없습니다."
    )
    @Transactional
    public PostResponse getPost(Long postId) {
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if ("DELETED".equals(post.getStatus())) {
            throw new IllegalArgumentException("삭제된 게시글입니다.");
        }

        post.incrementViewCount();
        return PostResponse.from(post);
    }

    @Operation(
            summary = "활성화된 게시글 전체 페이징 조회",
            description = "상태가 ACTIVE인 모든 커뮤니티 게시글 목록을 페이징 규격에 맞춰 조회합니다."
    )
    @Transactional(readOnly = true)
    public Page<PostResponse> getPosts(Pageable pageable) {
        return communityPostRepository.findByStatus("ACTIVE", pageable)
                .map(PostResponse::from);
    }

    @Operation(
            summary = "게시글 수정",
            description = "게시글 작성자 본인 여부를 검증한 후, 요청된 필드(카테고리, 지역, 제목, 내용 등)를 선택적으로 업데이트합니다."
    )
    @Transactional
    public PostResponse updatePost(Long userId, Long postId, PostUpdateRequest request) {
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!post.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        ServiceCategory category = post.getCategory();
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));
        }

        Location location = post.getLocation();
        if (request.getLocationId() != null) {
            location = locationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new IllegalArgumentException("지역을 찾을 수 없습니다."));
        }

        String boardType = request.getBoardType() != null ? request.getBoardType() : post.getBoardType();
        String title = request.getTitle() != null ? request.getTitle() : post.getTitle();
        String content = request.getContent() != null ? request.getContent() : post.getContent();

        post.updatePost(category, location, boardType, title, content);

        if (request.getImageFileIds() != null && !request.getImageFileIds().isEmpty()) {
            List<FileUpload> files = fileUploadRepository.findAllById(request.getImageFileIds());
            for (FileUpload file : files) {
                file.updateTarget("COMMUNITY_POST", post.getCommunityPostId());
            }
        }

        return PostResponse.from(post);
    }

    @Operation(
            summary = "게시글 소프트 삭제",
            description = "게시글 작성자 본인 여부를 검증한 후, 해당 게시글의 내부 상태값을 명시하여 노출되지 않도록 삭제 처리합니다."
    )
    @Transactional
    public void deletePost(Long userId, Long postId) {
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!post.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        post.softDelete();
    }
}