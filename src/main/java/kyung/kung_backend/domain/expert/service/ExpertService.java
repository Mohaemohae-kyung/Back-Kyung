package kyung.kung_backend.domain.expert.service;

import kyung.kung_backend.domain.expert.dto.ExpertProfileCreateRequest;
import kyung.kung_backend.domain.expert.dto.ExpertSearchResponse;
import kyung.kung_backend.domain.expert.dto.ExpertDetailResponse;
import kyung.kung_backend.domain.expert.entity.ExpertProfile;
import kyung.kung_backend.domain.expert.repository.ExpertProfileRepository;
import kyung.kung_backend.domain.expert.dto.ExpertProfileUpdateRequest;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.domain.category.entity.ServiceCategory;
import kyung.kung_backend.domain.category.repository.ServiceCategoryRepository;
import kyung.kung_backend.domain.location.entity.Location;
import kyung.kung_backend.domain.location.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpertService {

    private final ExpertProfileRepository expertProfileRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final LocationRepository locationRepository;

    public void createProfile(User user, ExpertProfileCreateRequest request) {

        if (expertProfileRepository.existsByUser(user)) {
            throw new IllegalArgumentException("이미 고수 프로필이 존재합니다.");
        }

        ServiceCategory mainCategory = serviceCategoryRepository.findById(request.getMainCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리가 존재하지 않습니다."));

        Location mainLocation = locationRepository.findById(request.getMainLocationId())
                .orElseThrow(() -> new IllegalArgumentException("지역이 존재하지 않습니다."));

        ExpertProfile expertProfile = new ExpertProfile(
                user,
                request.getDisplayName(),
                request.getIntroduction(),
                request.getCareerYears(),
                mainCategory,
                mainLocation
        );

        expertProfileRepository.save(expertProfile);
    }

    public void updateProfile(User user, ExpertProfileUpdateRequest request) {

        ExpertProfile expertProfile = expertProfileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("고수 프로필이 존재하지 않습니다."));

        ServiceCategory mainCategory = serviceCategoryRepository.findById(request.getMainCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리가 존재하지 않습니다."));

        Location mainLocation = locationRepository.findById(request.getMainLocationId())
                .orElseThrow(() -> new IllegalArgumentException("지역이 존재하지 않습니다."));

        expertProfile.updateProfile(
                request.getDisplayName(),
                request.getIntroduction(),
                request.getCareerYears(),
                mainCategory,
                mainLocation
        );
    }

    @Transactional(readOnly = true)
    public List<ExpertSearchResponse> searchExperts(
            Long categoryId,
            Long locationId,
            String keyword
    ) {

        List<ExpertProfile> expertProfiles =
                expertProfileRepository.findByStatus("ACTIVE");

        return expertProfiles.stream()
                .filter(expertProfile ->
                        categoryId == null ||
                                (
                                        expertProfile.getMainCategory() != null &&
                                                expertProfile.getMainCategory().getCategoryId().equals(categoryId)
                                )
                )
                .filter(expertProfile ->
                        locationId == null ||
                                (
                                        expertProfile.getMainLocation() != null &&
                                                expertProfile.getMainLocation().getLocationId().equals(locationId)
                                )
                )
                .filter(expertProfile ->
                        keyword == null ||
                                expertProfile.getDisplayName().contains(keyword) ||
                                expertProfile.getIntroduction().contains(keyword)
                )
                .map(ExpertSearchResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExpertDetailResponse getExpertDetail(Long expertId) {

        ExpertProfile expertProfile = expertProfileRepository.findById(expertId)
                .orElseThrow(() -> new IllegalArgumentException("고수 프로필이 존재하지 않습니다."));

        return ExpertDetailResponse.from(expertProfile);
    }
}