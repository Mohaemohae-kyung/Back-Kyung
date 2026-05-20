package kyung.kung_backend.domain.favorite.service;

import kyung.kung_backend.domain.expert.entity.ExpertProfile;
import kyung.kung_backend.domain.expert.repository.ExpertProfileRepository;
import kyung.kung_backend.domain.favorite.dto.FavoriteExpertResponse;
import kyung.kung_backend.domain.favorite.dto.FavoriteExpertToggleResponse;
import kyung.kung_backend.domain.favorite.entity.FavoriteExpert;
import kyung.kung_backend.domain.favorite.repository.FavoriteExpertRepository;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.global.exception.GeneralException;
import kyung.kung_backend.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteExpertService {

    private final FavoriteExpertRepository favoriteExpertRepository;
    private final ExpertProfileRepository expertProfileRepository;

    @Transactional
    public FavoriteExpertToggleResponse toggleFavorite(Long expertProfileId, User user) {
        ExpertProfile expertProfile = expertProfileRepository.findById(expertProfileId)
                .orElseThrow(() -> GeneralException.of(ErrorCode.EXPERT_PROFILE_NOT_FOUND));

        boolean isFavorite = favoriteExpertRepository.existsByUserAndExpertProfile(user, expertProfile);

        if (isFavorite) {
            favoriteExpertRepository.deleteByUserAndExpertProfile(user, expertProfile);

            return new FavoriteExpertToggleResponse(
                    expertProfile.getExpertProfileId(),
                    false
            );
        }

        FavoriteExpert favoriteExpert = FavoriteExpert.create(user, expertProfile);
        favoriteExpertRepository.save(favoriteExpert);

        return new FavoriteExpertToggleResponse(
                expertProfile.getExpertProfileId(),
                true
        );
    }

    public List<FavoriteExpertResponse> getMyFavoriteExperts(User user) {
        return favoriteExpertRepository.findAllByUserWithExpertProfile(user)
                .stream()
                .map(favoriteExpert -> FavoriteExpertResponse.from(favoriteExpert.getExpertProfile()))
                .toList();
    }
}