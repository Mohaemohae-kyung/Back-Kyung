package kyung.kung_backend.domain.notification.repository;

import kyung.kung_backend.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}