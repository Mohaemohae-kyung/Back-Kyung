package kyung.kung_backend.domain.expert.service;

import kyung.kung_backend.domain.expert.dto.ExpertProfileCreateRequest;
import kyung.kung_backend.domain.expert.dto.ExpertSearchResponse;
import kyung.kung_backend.domain.expert.dto.ExpertDetailResponse;
import kyung.kung_backend.domain.expert.entity.ExpertProfile;
import kyung.kung_backend.domain.expert.repository.ExpertProfileRepository;
import kyung.kung_backend.domain.expert.dto.ExpertProfileUpdateRequest;

import kyung.kung_backend.domain.servicepost.repository.ExpertServiceRepository;

import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.domain.user.repository.UserRepository;

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
    private final ExpertServiceRepository expertServiceRepository;

    private final ServiceCategoryRepository serviceCategoryRepository;
    private final LocationRepository locationRepository;

    private final UserRepository userRepository;
    
    
    @Transactional
    public void createProfile(
            User currentUser,
            ExpertProfileCreateRequest request
    ) {

        // 현재 로그인 유저 사용
        User user = currentUser;

        ServiceCategory mainCategory =
                serviceCategoryRepository
                        .findById(request.getMainCategoryId())
                        .orElseThrow(() ->
                                new IllegalArgumentException("카테고리가 존재하지 않습니다.")
                        );

        Location mainLocation =
                locationRepository
                        .findById(request.getMainLocationId())
                        .orElseThrow(() ->
                                new IllegalArgumentException("지역이 존재하지 않습니다.")
                        );

        // =========================
        // 이미 프로필 존재
        // → 수정 처리
        // =========================

        if (expertProfileRepository.existsByUser(user)) {

            ExpertProfile existingProfile =
                    expertProfileRepository.findByUser(user)
                            .orElseThrow(() ->
                                    new IllegalArgumentException("고수 프로필이 존재하지 않습니다.")
                            );

            existingProfile.updateProfile(
                    request.getDisplayName(),
                    request.getIntroduction(),
                    request.getCareerYears(),
                    mainCategory,
                    mainLocation
            );

            return;
        }

        // =========================
        // 신규 생성
        // =========================

        ExpertProfile expertProfile = new ExpertProfile(
                user,
                request.getDisplayName(),
                request.getIntroduction(),
                request.getCareerYears(),
                mainCategory,
                mainLocation
        );

        expertProfileRepository.save(expertProfile);

        // USER → EXPERT 변경
        user.becomeExpert();

        // USER 저장
        userRepository.save(user);
    }

    public void updateProfile(
            User user,
            ExpertProfileUpdateRequest request
    ) {

        ExpertProfile expertProfile =
                expertProfileRepository.findByUser(user)
                        .orElseThrow(() ->
                                new IllegalArgumentException("고수 프로필이 존재하지 않습니다.")
                        );

        ServiceCategory mainCategory =
                serviceCategoryRepository.findById(request.getMainCategoryId())
                        .orElseThrow(() ->
                                new IllegalArgumentException("카테고리가 존재하지 않습니다.")
                        );

        Location mainLocation =
                locationRepository.findById(request.getMainLocationId())
                        .orElseThrow(() ->
                                new IllegalArgumentException("지역이 존재하지 않습니다.")
                        );

        expertProfile.updateProfile(
                request.getDisplayName(),
                request.getIntroduction(),
                request.getCareerYears(),
                mainCategory,
                mainLocation
        );

        // =========================
        // ACTIVE 서비스 없으면 자동 생성
        // =========================

        boolean existsActiveExpertService =
                expertServiceRepository
                        .existsByExpertProfileAndCategory_CategoryIdAndStatus(
                                expertProfile,
                                mainCategory.getCategoryId(),
                                "ACTIVE"
                        );
    }

    @Transactional(readOnly = true)
    public List<ExpertSearchResponse> searchExperts(
            Long categoryId,
            Long locationId,
            String keyword
    ) {
        List<kyung.kung_backend.domain.servicepost.entity.ExpertService> expertServices =
                expertServiceRepository.findByStatus("ACTIVE");

        return expertServices.stream()

                .filter(expertService ->
                        "ACTIVE".equals(expertService.getExpertProfile().getStatus())
                )

                .filter(expertService ->
                        !"DELETED".equals(expertService.getExpertProfile().getUser().getStatus())
                )

                .filter(expertService ->
                        categoryId == null ||
                                (
                                        expertService.getCategory() != null &&
                                                expertService.getCategory().getCategoryId().equals(categoryId)
                                )
                )

                .filter(expertService ->
                        locationId == null ||
                                (
                                        expertService.getExpertProfile().getMainLocation() != null &&
                                                expertService.getExpertProfile().getMainLocation().getLocationId().equals(locationId)
                                )
                )

                .filter(expertService ->
                        keyword == null ||

                                expertService.getExpertProfile().getDisplayName().contains(keyword) ||

                                expertService.getServiceTitle().contains(keyword) ||

                                expertService.getServiceDescription().contains(keyword)
                )
                .map(ExpertSearchResponse::from)

                .toList();
    }

    @Transactional(readOnly = true)
    public ExpertDetailResponse getExpertDetail(Long serviceId) {

        kyung.kung_backend.domain.servicepost.entity.ExpertService expertService =
                expertServiceRepository.findDetailById(serviceId)
                        .orElseThrow(() ->
                                new IllegalArgumentException("고수 서비스가 존재하지 않습니다.")
                        );

        if (!"ACTIVE".equals(expertService.getStatus()) ||
                !"ACTIVE".equals(expertService.getExpertProfile().getStatus()) ||
                "DELETED".equals(expertService.getExpertProfile().getUser().getStatus())) {
            throw new IllegalArgumentException("고수 서비스가 존재하지 않습니다.");
        }

        // =========================
        // 견적 요청용 서비스 ID 목록
        // =========================

        List<Long> expertServiceIds =
                expertServiceRepository
                        .findAllByExpertProfileAndStatus(
                                expertService.getExpertProfile(),
                                "ACTIVE"
                        )
                        .stream()
                        .map(
                                kyung.kung_backend.domain.servicepost.entity.ExpertService::getExpertServiceId
                        )
                        .toList();

        return ExpertDetailResponse.from(
                expertService,
                expertServiceIds
        );
    }
}