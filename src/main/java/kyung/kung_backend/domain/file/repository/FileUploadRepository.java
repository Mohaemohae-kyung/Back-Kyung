package kyung.kung_backend.domain.file.repository;

import kyung.kung_backend.domain.file.entity.FileUpload;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileUploadRepository extends JpaRepository<FileUpload, Long> {

    List<FileUpload> findByTargetTypeAndTargetIdAndStatus(String targetType, Long targetId, String status);

    List<FileUpload> findByTargetTypeAndTargetIdInAndStatus(String targetType, List<Long> targetIds, String status);
}