package kyung.kung_backend.domain.admin.entity;

import jakarta.persistence.*;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.common.BaseCreatedEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "ADMIN_ACTIONS")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "ADMIN_ACTIONS_SEQ_GENERATOR",
        sequenceName = "ADMIN_ACTIONS_SEQ",
        allocationSize = 1
)
public class AdminAction extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ADMIN_ACTIONS_SEQ_GENERATOR")
    @Column(name = "ADMIN_ACTION_ID", nullable = false)
    private Long adminActionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ADMIN_ID", nullable = false)
    private User admin;

    @Column(name = "TARGET_TYPE", nullable = false, length = 30)
    private String targetType;

    @Column(name = "TARGET_ID", nullable = false)
    private Long targetId;

    @Column(name = "ACTION_TYPE", nullable = false, length = 30)
    private String actionType;

    @Column(name = "REASON", length = 500)
    private String reason;

    public static AdminAction create(User admin, String targetType, Long targetId, String actionType, String reason) {
        AdminAction adminAction = new AdminAction();
        adminAction.admin = admin;
        adminAction.targetType = targetType;
        adminAction.targetId = targetId;
        adminAction.actionType = actionType;
        adminAction.reason = reason;
        return adminAction;
    }
}