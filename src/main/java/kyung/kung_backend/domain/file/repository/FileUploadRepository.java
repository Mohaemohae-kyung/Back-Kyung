package kyung.kung_backend.domain.file.repository;

import kyung.kung_backend.domain.file.entity.FileUpload;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileUploadRepository extends JpaRepository<FileUpload, Long> {
}