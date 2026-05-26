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
import kyung.kung_backend.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
    private final UserRepository userRepository;
    
    @PersistenceContext
    private final EntityManager em;

    @Transactional
    public void createProfile(User currentUser, ExpertProfileCreateRequest request) {
        User user = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

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

        user.becomeExpert();
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
        StringBuilder jpql = new StringBuilder("SELECT ep FROM ExpertProfile ep WHERE ep.status = 'ACTIVE'");
        
        if (categoryId != null) {
            jpql.append(" AND ep.mainCategory.categoryId = ").append(categoryId);
        }
        if (locationId != null) {
            jpql.append(" AND ep.mainLocation.locationId = ").append(locationId);
        }
        if (keyword != null && !keyword.isBlank()) {
            jpql.append(" AND (ep.displayName LIKE '%").append(keyword).append("%'")
                .append(" OR ep.introduction LIKE '%").append(keyword).append("%')");
        }

        List<ExpertProfile> expertProfiles = em.createQuery(jpql.toString(), ExpertProfile.class)
                                               .getResultList();

        return expertProfiles.stream()
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