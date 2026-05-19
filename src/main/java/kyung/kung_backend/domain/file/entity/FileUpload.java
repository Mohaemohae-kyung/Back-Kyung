package kyung.kung_backend.domain.file.entity;

import jakarta.persistence.*;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "FILE_UPLOADS")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "FILE_UPLOADS_SEQ_GENERATOR",
        sequenceName = "FILE_UPLOADS_SEQ",
        allocationSize = 1
)
public class FileUpload extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FILE_UPLOADS_SEQ_GENERATOR")
    @Column(name = "FILE_ID", nullable = false)
    private Long fileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UPLOADER_ID", nullable = false)
    private User uploader;

    @Column(name = "TARGET_TYPE", length = 50)
    private String targetType;

    @Column(name = "TARGET_ID")
    private Long targetId;

    @Column(name = "ORIGINAL_NAME", nullable = false, length = 255)
    private String originalName;

    @Column(name = "STORED_NAME", nullable = false, length = 255)
    private String storedName;

    @Column(name = "FILE_URL", nullable = false, length = 500)
    private String fileUrl;

    @Column(name = "CONTENT_TYPE", length = 100)
    private String contentType;

    @Column(name = "FILE_SIZE")
    private Long fileSize;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;

    @Builder
    public FileUpload(User uploader, String targetType, String originalName, String storedName, String fileUrl, String contentType, Long fileSize) {
        this.uploader = uploader;
        this.targetType = targetType;
        this.originalName = originalName;
        this.storedName = storedName;
        this.fileUrl = fileUrl;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.status = "ACTIVE";
    }

    // 도메인 타입과 식별자를 동시에 갱신하도록 수정
    public void updateTarget(String targetType, Long targetId) {
        this.targetType = targetType;
        this.targetId = targetId;
    }

    public void delete() {
        this.status = "DELETED";
    }
}