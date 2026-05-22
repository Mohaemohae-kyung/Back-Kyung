package kyung.kung_backend.domain.community.service;

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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityPostService {

    private final CommunityPostRepository communityPostRepository;
    private final UserRepository userRepository;
    private final ServiceCategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final FileUploadRepository fileUploadRepository;
    private final DataSource dataSource;

    // 컬럼명 화이트리스트 검증용 해시맵
    private static final Map<String, String> SORT_COLUMN_MAP = Map.of(
            "postId", "p.COMMUNITY_POST_ID",
            "title", "p.TITLE",
            "viewCount", "p.VIEW_COUNT"
    );

    // 정렬 방향 화이트리스트 검증용 해시맵
    // private static final Map<String, String> SORT_DIRECTION_MAP = Map.of(
    //         "asc", "ASC",
    //         "desc", "DESC"
    // );

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

        List<String> fileUrls = Collections.emptyList();
        if (request.getImageFileIds() != null && !request.getImageFileIds().isEmpty()) {
            List<FileUpload> files = fileUploadRepository.findAllById(request.getImageFileIds());
            for (FileUpload file : files) {
                file.updateTarget("COMMUNITY_POST", savedPost.getCommunityPostId());
            }
            fileUrls = files.stream().map(FileUpload::getFileUrl).collect(Collectors.toList());
        }

        return PostResponse.from(savedPost, fileUrls);
    }

    @Transactional
    public PostResponse getPost(Long postId) {
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if ("DELETED".equals(post.getStatus())) {
            throw new IllegalArgumentException("삭제된 게시글입니다.");
        }

        post.incrementViewCount();

        List<FileUpload> files = fileUploadRepository.findByTargetTypeAndTargetIdAndStatus("COMMUNITY_POST", postId, "ACTIVE");
        List<String> fileUrls = files.stream().map(FileUpload::getFileUrl).collect(Collectors.toList());

        return PostResponse.from(post, fileUrls);
    }

    // SQLI 취약점 존재 코드
    @Transactional(readOnly = true)
    public Page<PostResponse> getPosts(Pageable pageable, String sortColumn, String sortDirection) {
        List<Map<String, Object>> postDataList = new ArrayList<>();
        List<Long> postIds = new ArrayList<>();
        long total = 0;

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT p.COMMUNITY_POST_ID, p.TITLE, p.CONTENT, p.VIEW_COUNT, u.NAME AS WRITER_NAME ");
        queryBuilder.append("FROM COMMUNITY_POSTS p ");
        queryBuilder.append("JOIN USERS u ON p.USER_ID = u.USER_ID ");
        queryBuilder.append("WHERE p.STATUS = 'ACTIVE' ");

        String orderColumn = "p.COMMUNITY_POST_ID";
        String orderDirection = "DESC";

        // 컬럼명 화이트리스트 기반 안전한 치환
        if (sortColumn != null && !sortColumn.trim().isEmpty()) {
            orderColumn = SORT_COLUMN_MAP.getOrDefault(sortColumn.trim(), "p.COMMUNITY_POST_ID");
        }

        // 방향 입력값 검증 누락 (취약점)
        if (sortDirection != null && !sortDirection.trim().isEmpty()) {
            orderDirection = sortDirection;
        }

        // 방향 입력값 검증 (시큐어 코딩 적용 시 참고용)
        // if (sortDirection != null && !sortDirection.trim().isEmpty()) {
        //     orderDirection = SORT_DIRECTION_MAP.getOrDefault(sortDirection.trim().toLowerCase(), "DESC");
        // }

        // 쿼리 결합
        queryBuilder.append("ORDER BY ").append(orderColumn).append(" ").append(orderDirection).append(" ");
        queryBuilder.append("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        String countQuery = "SELECT COUNT(*) FROM COMMUNITY_POSTS WHERE STATUS = 'ACTIVE'";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(queryBuilder.toString());
             PreparedStatement countPstmt = conn.prepareStatement(countQuery)) {

            pstmt.setLong(1, pageable.getOffset());
            pstmt.setInt(2, pageable.getPageSize());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> data = new HashMap<>();
                    Long postId = rs.getLong("COMMUNITY_POST_ID");
                    data.put("postId", postId);
                    data.put("title", rs.getString("TITLE"));
                    data.put("content", rs.getString("CONTENT"));
                    data.put("viewCount", rs.getLong("VIEW_COUNT"));
                    data.put("writerName", rs.getString("WRITER_NAME"));

                    postIds.add(postId);
                    postDataList.add(data);
                }
            }

            try (ResultSet rsCount = countPstmt.executeQuery()) {
                if (rsCount.next()) {
                    total = rsCount.getLong(1);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("게시글 목록 조회 중 데이터베이스 오류가 발생했습니다.", e);
        }

        List<FileUpload> files = fileUploadRepository.findByTargetTypeAndTargetIdInAndStatus("COMMUNITY_POST", postIds, "ACTIVE");
        Map<Long, List<String>> fileUrlMap = files.stream()
                .collect(Collectors.groupingBy(
                        FileUpload::getTargetId,
                        Collectors.mapping(FileUpload::getFileUrl, Collectors.toList())
                ));

        List<PostResponse> posts = postDataList.stream().map(data -> {
            Long id = (Long) data.get("postId");
            return PostResponse.builder()
                    .postId(id)
                    .title((String) data.get("title"))
                    .content((String) data.get("content"))
                    .viewCount((Long) data.get("viewCount"))
                    .writerName((String) data.get("writerName"))
                    .imageUrls(fileUrlMap.getOrDefault(id, Collections.emptyList()))
                    .build();
        }).collect(Collectors.toList());

        return new PageImpl<>(posts, pageable, total);
    }

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

        List<FileUpload> updatedFiles = fileUploadRepository.findByTargetTypeAndTargetIdAndStatus("COMMUNITY_POST", postId, "ACTIVE");
        List<String> fileUrls = updatedFiles.stream().map(FileUpload::getFileUrl).collect(Collectors.toList());

        return PostResponse.from(post, fileUrls);
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