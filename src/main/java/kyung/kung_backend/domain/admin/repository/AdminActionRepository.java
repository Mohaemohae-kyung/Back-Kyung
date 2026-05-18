package kyung.kung_backend.domain.admin.repository;

import kyung.kung_backend.domain.admin.entity.AdminAction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminActionRepository extends JpaRepository<AdminAction, Long> {
}