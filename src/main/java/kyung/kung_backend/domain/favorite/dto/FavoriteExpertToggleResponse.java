package kyung.kung_backend.domain.favorite.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FavoriteExpertToggleResponse {

    private Long expertProfileId;
    private boolean favorite;
}
