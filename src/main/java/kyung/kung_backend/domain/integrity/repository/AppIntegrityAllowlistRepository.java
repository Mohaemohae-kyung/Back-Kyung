package kyung.kung_backend.domain.integrity.repository;

import kyung.kung_backend.domain.integrity.entity.AppIntegrityAllowlistEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppIntegrityAllowlistRepository
        extends JpaRepository<AppIntegrityAllowlistEntry, Long> {

    List<AppIntegrityAllowlistEntry> findAllByPackageNameAndVersionCodeAndBuildType(
            String packageName,
            Long versionCode,
            String buildType
    );

    Optional<AppIntegrityAllowlistEntry> findByPackageNameAndVersionCodeAndBuildTypeAndSha256(
            String packageName,
            Long versionCode,
            String buildType,
            String sha256
    );
}
