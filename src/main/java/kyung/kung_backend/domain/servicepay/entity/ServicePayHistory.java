package kyung.kung_backend.domain.servicepay.entity;

import jakarta.persistence.*;
import kyung.kung_backend.domain.transaction.entity.Transaction;
import kyung.kung_backend.global.common.BaseCreatedEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "SERVICE_PAY_HISTORIES")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "SERVICE_PAY_HISTORIES_SEQ_GENERATOR",
        sequenceName = "SERVICE_PAY_HISTORIES_SEQ",
        allocationSize = 1
)
public class ServicePayHistory extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SERVICE_PAY_HISTORIES_SEQ_GENERATOR")
    @Column(name = "SERVICE_PAY_HISTORY_ID", nullable = false)
    private Long servicePayHistoryId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "SERVICE_PAY_ACCOUNT_ID", nullable = false)
    private ServicePayAccount servicePayAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TRANSACTION_ID")
    private Transaction transaction;

    @Column(name = "HISTORY_TYPE", nullable = false, length = 30)
    private String historyType;

    @Column(name = "AMOUNT", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "BALANCE_AFTER", nullable = false, precision = 12, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "DESCRIPTION", length = 255)
    private String description;
}