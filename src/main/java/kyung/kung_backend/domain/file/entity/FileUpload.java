package kyung.kung_backend.domain.file.entity;

import jakarta.persistence.*;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.common.BaseCreatedEntity;
import lombok.AccessLevel;
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
public class FileUpload extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FILE_UPLOADS_SEQ_GENERATOR")
    @Column(name = "FILE_ID", nullable = false)
    private Long fileId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "UPLOADER_ID", nullable = false)
    private User uploader;

    @Column(name = "TARGET_TYPE", nullable = false, length = 30)
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
}