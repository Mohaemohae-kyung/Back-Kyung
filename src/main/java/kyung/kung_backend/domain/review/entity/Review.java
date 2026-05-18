package kyung.kung_backend.domain.review.entity;

import jakarta.persistence.*;
import kyung.kung_backend.domain.booking.entity.Booking;
import kyung.kung_backend.domain.expert.entity.ExpertProfile;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "REVIEWS")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "REVIEWS_SEQ_GENERATOR",
        sequenceName = "REVIEWS_SEQ",
        allocationSize = 1
)
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "REVIEWS_SEQ_GENERATOR")
    @Column(name = "REVIEW_ID", nullable = false)
    private Long reviewId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "BOOKING_ID", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "REVIEWER_ID", nullable = false)
    private User reviewer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "EXPERT_PROFILE_ID", nullable = false)
    private ExpertProfile expertProfile;

    @Column(name = "RATING", nullable = false)
    private Long rating;

    @Lob
    @Column(name = "CONTENT")
    private String content;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;

    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;
}