package kyung.kung_backend.domain.location.entity;

import jakarta.persistence.*;
import kyung.kung_backend.global.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "LOCATIONS")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "LOCATIONS_SEQ_GENERATOR",
        sequenceName = "LOCATIONS_SEQ",
        allocationSize = 1
)
public class Location extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "LOCATIONS_SEQ_GENERATOR")
    @Column(name = "LOCATION_ID", nullable = false)
    private Long locationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_ID")
    private Location parent;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "DEPTH", nullable = false)
    private Long depth;

    @Column(name = "LATITUDE", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "LONGITUDE", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "ACTIVE_YN", nullable = false, length = 1)
    private String activeYn;
}