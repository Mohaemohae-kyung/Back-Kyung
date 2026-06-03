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

    @Column(name = "EXPERT_PROFILE_IMAGE_STORED_NAME", length = 500)
    private String expertProfileImageStoredName;

    @Column(name = "EXPERT_PROFILE_IMAGE_ORIGINAL_NAME", length = 255)
    private String expertProfileImageOriginalName;

    @Column(name = "EXPERT_PROFILE_IMAGE_CONTENT_TYPE", length = 100)
    private String expertProfileImageContentType;

    @Column(name = "EXPERT_PROFILE_IMAGE_SIZE")
    private Long expertProfileImageSize;

    @Column(name = "VERIFIED_YN", nullable = false, length = 1)
    private String verifiedYn;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;

    // 포트폴리오 외부 경로 필드 추가
    @Column(name = "EXTERNAL_PORTFOLIO_URL", length = 1000)
    private String externalPortfolioUrl;

    public ExpertProfile(
            User user,
            String displayName,
            String introduction,
            Double careerYears,
            ServiceCategory mainCategory,
            Location mainLocation,
            String externalPortfolioUrl
    ) {
        this.user = user;
        this.displayName = displayName;
        this.introduction = introduction;
        this.careerYears = careerYears;
        this.mainCategory = mainCategory;
        this.mainLocation = mainLocation;
        this.externalPortfolioUrl = externalPortfolioUrl;
        this.verifiedYn = "N";
        this.status = "ACTIVE";
    }

    public void updateProfile(
            String displayName,
            String introduction,
            Double careerYears,
            ServiceCategory mainCategory,
            Location mainLocation,
            String externalPortfolioUrl
    ) {
        this.displayName = displayName;
        this.introduction = introduction;
        this.careerYears = careerYears;
        this.mainCategory = mainCategory;
        this.mainLocation = mainLocation;
        this.externalPortfolioUrl = externalPortfolioUrl;
    }

    public void delete() {
        this.status = "DELETED";
    }

    public void updateExpertProfileImage(
            String storedName,
            String originalName,
            String contentType,
            Long size
    ) {
        this.expertProfileImageStoredName = storedName;
        this.expertProfileImageOriginalName = originalName;
        this.expertProfileImageContentType = contentType;
        this.expertProfileImageSize = size;
    }
}
