package kyung.kung_backend.domain.integrity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import kyung.kung_backend.global.common.BaseCreatedEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "APP_INTEGRITY_ALLOWLIST_SEQ_GENERATOR",
        sequenceName = "APP_INTEGRITY_ALLOWLIST_SEQ",
        allocationSize = 1
)
@Table(
        name = "APP_INTEGRITY_ALLOWLIST",
        uniqueConstraints = @UniqueConstraint(
                name = "UK_APP_INTEGRITY_ALLOWLIST",
                columnNames = {"PACKAGE_NAME", "VERSION_CODE", "BUILD_TYPE", "SHA256"}
        ),
        indexes = @Index(
                name = "IDX_APP_INTEGRITY_LOOKUP",
                columnList = "PACKAGE_NAME,VERSION_CODE,BUILD_TYPE"
        )
)
public class AppIntegrityAllowlistEntry extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "APP_INTEGRITY_ALLOWLIST_SEQ_GENERATOR")
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "PACKAGE_NAME", nullable = false, length = 255)
    private String packageName;

    @Column(name = "VERSION_CODE", nullable = false)
    private Long versionCode;

    @Column(name = "BUILD_TYPE", nullable = false, length = 32)
    private String buildType;

    @Column(name = "SHA256", nullable = false, length = 64)
    private String sha256;

    @Column(name = "NOTE", length = 255)
    private String note;

    public static AppIntegrityAllowlistEntry of(
            String packageName,
            Long versionCode,
            String buildType,
            String sha256,
            String note
    ) {
        AppIntegrityAllowlistEntry entry = new AppIntegrityAllowlistEntry();
        entry.packageName = packageName;
        entry.versionCode = versionCode;
        entry.buildType = buildType;
        entry.sha256 = sha256;
        entry.note = note;
        return entry;
    }
}
