package kyung.kung_backend.domain.community.dto;

import kyung.kung_backend.domain.community.entity.CommunityPost;
import kyung.kung_backend.domain.file.entity.FileUpload;
import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class PostResponse {

    private Long postId;
    private String title;
    private String content;
    private Long viewCount;
    private String writerName;

    // 기존 프론트 호환용
    private List<String> imageUrls;

    // 원본 파일명 표시용
    private List<PostFileResponse> files;

    public static PostResponse from(CommunityPost post, List<FileUpload> files) {
        List<FileUpload> safeFiles = files == null ? Collections.emptyList() : files;

        return PostResponse.builder()
                .postId(post.getCommunityPostId())
                .title(post.getTitle())
                .content(post.getContent())
                .viewCount(post.getViewCount())
                .writerName(post.getUser().getName())
                .imageUrls(
                        safeFiles.stream()
                                .map(FileUpload::getFileUrl)
                                .collect(Collectors.toList())
                )
                .files(
                        safeFiles.stream()
                                .map(PostFileResponse::from)
                                .collect(Collectors.toList())
                )
                .build();
    }
}