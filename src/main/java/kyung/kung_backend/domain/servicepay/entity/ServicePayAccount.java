package kyung.kung_backend.domain.servicepay.entity;

import jakarta.persistence.*;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@Table(
        name = "SERVICE_PAY_ACCOUNTS",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_SERVICE_PAY_ACCOUNTS_USER", columnNames = "USER_ID")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "SERVICE_PAY_ACCOUNTS_SEQ_GENERATOR",
        sequenceName = "SERVICE_PAY_ACCOUNTS_SEQ",
        allocationSize = 1
)
public class ServicePayAccount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SERVICE_PAY_ACCOUNTS_SEQ_GENERATOR")
    @Column(name = "SERVICE_PAY_ACCOUNT_ID", nullable = false)
    private Long servicePayAccountId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false, unique = true)
    private User user;

    @Column(name = "BALANCE", nullable = false, precision = 12, scale = 2)
    private BigDecimal balance;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;
}