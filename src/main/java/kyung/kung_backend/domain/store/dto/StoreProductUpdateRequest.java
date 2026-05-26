package kyung.kung_backend.domain.store.dto;

import kyung.kung_backend.domain.store.entity.enums.StoreProductServiceType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class StoreProductUpdateRequest {

    private Long categoryId;

    private String title;

    private Long thumbnailImageFileId;

    private String description;

    private BigDecimal price;

    private StoreProductServiceType serviceType;

    private Long locationId;
}
