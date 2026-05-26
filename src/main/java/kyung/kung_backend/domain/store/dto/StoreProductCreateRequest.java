package kyung.kung_backend.domain.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import kyung.kung_backend.domain.store.entity.enums.StoreProductServiceType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class StoreProductCreateRequest {

    @NotNull(message = "카테고리 ID는 필수입니다.")
    private Long categoryId;

    @NotBlank(message = "상품명은 필수입니다.")
    private String title;

    @NotNull(message = "대표 이미지 파일 ID는 필수입니다.")
    private Long thumbnailImageFileId;

    @NotBlank(message = "상세 설명은 필수입니다.")
    private String description;

    @NotNull(message = "가격은 필수입니다.")
    @PositiveOrZero(message = "가격은 0원 이상이어야 합니다.")
    private BigDecimal price;

    @NotNull(message = "진행 방식은 필수입니다.")
    private StoreProductServiceType serviceType;

    private String serviceRegion;
}