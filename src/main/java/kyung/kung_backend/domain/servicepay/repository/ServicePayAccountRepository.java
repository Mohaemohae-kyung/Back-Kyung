package kyung.kung_backend.domain.servicepay.repository;

import kyung.kung_backend.domain.servicepay.entity.ServicePayAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicePayAccountRepository extends JpaRepository<ServicePayAccount, Long> {
}