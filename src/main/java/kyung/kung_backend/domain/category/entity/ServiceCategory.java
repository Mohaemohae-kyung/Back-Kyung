package kyung.kung_backend.domain.category.entity;

import jakarta.persistence.*;
import kyung.kung_backend.global.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "SERVICE_CATEGORIES")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "SERVICE_CATEGORIES_SEQ_GENERATOR",
        sequenceName = "SERVICE_CATEGORIES_SEQ",
        allocationSize = 1
)
public class ServiceCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SERVICE_CATEGORIES_SEQ_GENERATOR")
    @Column(name = "CATEGORY_ID", nullable = false)
    private Long categoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_ID")
    private ServiceCategory parent;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "DEPTH", nullable = false)
    private Long depth;

    @Column(name = "SORT_ORDER", nullable = false)
    private Long sortOrder;

    @Column(name = "ACTIVE_YN", nullable = false, length = 1)
    private String activeYn;
}