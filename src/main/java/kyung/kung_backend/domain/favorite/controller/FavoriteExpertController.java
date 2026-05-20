package kyung.kung_backend.domain.favorite.controller;

import kyung.kung_backend.domain.favorite.dto.FavoriteExpertToggleResponse;
import kyung.kung_backend.domain.favorite.service.FavoriteExpertService;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.response.ApiResponse;
import kyung.kung_backend.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/experts")
public class FavoriteExpertController {

    private final FavoriteExpertService favoriteExpertService;

    @PostMapping("/{expertProfileId}/favorite")
    public ApiResponse<FavoriteExpertToggleResponse> toggleFavorite(
            @PathVariable Long expertProfileId,
            @AuthenticationPrincipal User user
    ) {
        FavoriteExpertToggleResponse response = favoriteExpertService.toggleFavorite(expertProfileId, user);
        return ApiResponse.onSuccess(SuccessCode.FAVORITE_EXPERT_TOGGLE_SUCCESS, response);
    }
}