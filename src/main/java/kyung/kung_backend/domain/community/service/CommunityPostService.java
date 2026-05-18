package kyung.kung_backend.domain.community.service;

import kyung.kung_backend.domain.category.entity.ServiceCategory;
import kyung.kung_backend.domain.category.repository.ServiceCategoryRepository;
import kyung.kung_backend.domain.community.dto.PostCreateRequest;
import kyung.kung_backend.domain.community.dto.PostResponse;
import kyung.kung_backend.domain.community.dto.PostUpdateRequest;
import kyung.kung_backend.domain.community.entity.CommunityPost;
import kyung.kung_backend.domain.community.repository.CommunityPostRepository;
import kyung.kung_backend.domain.location.entity.Location;
import kyung.kung_backend.domain.location.repository.LocationRepository;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommunityPostService {

    private final CommunityPostRepository communityPostRepository;
    private final UserRepository userRepository;
    private final ServiceCategoryRepository categoryRepository;
    private final LocationRepository locationRepository;

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
        return PostResponse.from(savedPost);
    }

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

    @Transactional(readOnly = true)
    public Page<PostResponse> getPosts(Pageable pageable) {
        return communityPostRepository.findAll(pageable)
                .map(PostResponse::from);
    }

    @Transactional
    public PostResponse updatePost(Long userId, Long postId, PostUpdateRequest request) {
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!post.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

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

        post.updatePost(category, location, request.getBoardType(), request.getTitle(), request.getContent());

        return PostResponse.from(post);
    }

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