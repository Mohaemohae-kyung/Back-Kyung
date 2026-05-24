package kyung.kung_backend.domain.expert.entity;

import jakarta.persistence.*;
import kyung.kung_backend.domain.category.entity.ServiceCategory;
import kyung.kung_backend.domain.file.entity.FileUpload;
import kyung.kung_backend.domain.location.entity.Location;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "EXPERT_PROFILES")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "EXPERT_PROFILES_SEQ_GENERATOR",
        sequenceName = "EXPERT_PROFILES_SEQ",
        allocationSize = 1
)
public class ExpertProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "EXPERT_PROFILES_SEQ_GENERATOR")
    @Column(name = "EXPERT_PROFILE_ID", nullable = false)
    private Long expertProfileId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false, unique = true)
    private User user;

    @Column(name = "DISPLAY_NAME", nullable = false, length = 100)
    private String displayName;

    @Lob
    @Column(name = "INTRODUCTION")
    private String introduction;

    @Column(name = "CAREER_YEARS")
    private Double careerYears;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MAIN_CATEGORY_ID")
    private ServiceCategory mainCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MAIN_LOCATION_ID")
    private Location mainLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROFILE_FILE_ID")
    private FileUpload profileFile;

    @Column(name = "VERIFIED_YN", nullable = false, length = 1)
    private String verifiedYn;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;

    public ExpertProfile(
            User user,
            String displayName,
            String introduction,
            Double careerYears,
            ServiceCategory mainCategory,
            Location mainLocation
    ) {
        this.user = user;
        this.displayName = displayName;
        this.introduction = introduction;
        this.careerYears = careerYears;
        this.mainCategory = mainCategory;
        this.mainLocation = mainLocation;
        this.verifiedYn = "N";
        this.status = "ACTIVE";
    }

    public void updateProfile(
            String displayName,
            String introduction,
            Double careerYears,
            ServiceCategory mainCategory,
            Location mainLocation
    ) {
        this.displayName = displayName;
        this.introduction = introduction;
        this.careerYears = careerYears;
        this.mainCategory = mainCategory;
        this.mainLocation = mainLocation;
    }

    public void delete() {
        this.status = "DELETED";
    }
}