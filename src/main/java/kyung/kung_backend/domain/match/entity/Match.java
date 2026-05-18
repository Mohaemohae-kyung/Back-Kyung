package kyung.kung_backend.domain.match.entity;

import jakarta.persistence.*;
import kyung.kung_backend.domain.expert.entity.ExpertProfile;
import kyung.kung_backend.domain.request.entity.ServiceRequest;
import kyung.kung_backend.global.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "MATCHES")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "MATCHES_SEQ_GENERATOR",
        sequenceName = "MATCHES_SEQ",
        allocationSize = 1
)
public class Match extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MATCHES_SEQ_GENERATOR")
    @Column(name = "MATCH_ID", nullable = false)
    private Long matchId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "REQUEST_ID", nullable = false)
    private ServiceRequest serviceRequest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "EXPERT_PROFILE_ID", nullable = false)
    private ExpertProfile expertProfile;

    @Column(name = "PROPOSED_PRICE", precision = 12, scale = 2)
    private BigDecimal proposedPrice;

    @Lob
    @Column(name = "MESSAGE")
    private String message;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;
}