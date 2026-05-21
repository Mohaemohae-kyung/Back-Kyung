package kyung.kung_backend.domain.file.repository;

import kyung.kung_backend.domain.file.entity.FileUpload;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileUploadRepository extends JpaRepository<FileUpload, Long> {

    List<FileUpload> findByTargetTypeAndTargetIdAndStatus(String targetType, Long targetId, String status);

    List<FileUpload> findByTargetTypeAndTargetIdInAndStatus(String targetType, List<Long> targetIds, String status);

    // storedName 기반 조회 메서드 추가
    Optional<FileUpload> findByStoredName(String storedName);
}