package kyung.kung_backend.domain.expert.service;

import kyung.kung_backend.domain.category.entity.ServiceCategory;
import kyung.kung_backend.domain.category.repository.ServiceCategoryRepository;
import kyung.kung_backend.domain.expert.dto.ExpertDetailResponse;
import kyung.kung_backend.domain.expert.dto.ExpertProfileCreateRequest;
import kyung.kung_backend.domain.expert.dto.ExpertProfileUpdateRequest;
import kyung.kung_backend.domain.expert.dto.ExpertSearchResponse;
import kyung.kung_backend.domain.expert.entity.ExpertProfile;
import kyung.kung_backend.domain.expert.repository.ExpertProfileRepository;
import kyung.kung_backend.domain.location.entity.Location;
import kyung.kung_backend.domain.location.repository.LocationRepository;
import kyung.kung_backend.domain.servicepost.repository.ExpertServiceRepository;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
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
        log.info("[createProfile] start userId={}", currentUser.getUserId());

        if (expertProfileRepository.existsByUser(currentUser)) {
            log.warn("[createProfile] already exists userId={}", currentUser.getUserId());
            throw new IllegalArgumentException("이미 고수 프로필이 존재합니다. 수정 API를 사용해주세요.");
        }

        ServiceCategory mainCategory =
                serviceCategoryRepository
                        .findById(request.getMainCategoryId())
                        .orElseThrow(() ->
                                new IllegalArgumentException("카테고리가 존재하지 않습니다.")
                        );

        log.info("[createProfile] mainCategory loaded categoryId={}", mainCategory.getCategoryId());

        Location mainLocation =
                locationRepository
                        .findById(request.getMainLocationId())
                        .orElseThrow(() ->
                                new IllegalArgumentException("지역이 존재하지 않습니다.")
                        );

        log.info("[createProfile] mainLocation loaded locationId={}", mainLocation.getLocationId());

        ExpertProfile expertProfile = new ExpertProfile(
                currentUser,
                request.getDisplayName(),
                request.getIntroduction(),
                request.getCareerYears(),
                mainCategory,
                mainLocation,
                request.getExternalPortfolioUrl()
        );

        expertProfileRepository.save(expertProfile);

        log.info("[createProfile] expertProfile saved profileId={}", expertProfile.getExpertProfileId());

        saveExpertServiceMappings(
                expertProfile,
                mainCategory,
                request.getCategoryIds()
        );

        currentUser.becomeExpert();
        userRepository.save(currentUser);

        log.info("[createProfile] done userId={}, profileId={}",
                currentUser.getUserId(),
                expertProfile.getExpertProfileId()
        );
    }

    @Transactional
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

        updateExpertProfileAndServiceMapping(
                expertProfile,
                request.getDisplayName(),
                request.getIntroduction(),
                request.getCareerYears(),
                mainCategory,
                mainLocation,
                request.getExternalPortfolioUrl(),
                request.getCategoryIds()
        );
    }

    @Transactional(readOnly = true)
    public List<ExpertSearchResponse> searchExperts(
            Long categoryId,
            Long locationId,
            String keyword
    ) {
        List<kyung.kung_backend.domain.servicepost.entity.ExpertService> expertServices =
                expertServiceRepository.findAll();

        return expertServices.stream()

                // 고수 프로필 기준으로 그룹핑
                .collect(Collectors.groupingBy(
                        expertService -> expertService.getExpertProfile().getExpertProfileId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ))
                .values()
                .stream()

                // 고수 프로필 + 보유 카테고리 목록으로 변환
                .map(services -> {
                    ExpertProfile expertProfile =
                            services.get(0).getExpertProfile();

                    List<String> categoryNames =
                            services.stream()
                                    .map(kyung.kung_backend.domain.servicepost.entity.ExpertService::getCategory)
                                    .filter(Objects::nonNull)
                                    .map(category -> category.getName())
                                    .distinct()
                                    .toList();

                    return new AbstractMap.SimpleEntry<>(expertProfile, categoryNames);
                })

                // 고수 프로필 상태 기준
                .filter(entry ->
                        "ACTIVE".equals(entry.getKey().getStatus())
                )

                // 탈퇴/삭제 유저 제외
                .filter(entry ->
                        entry.getKey().getUser() != null &&
                                !"DELETED".equals(entry.getKey().getUser().getStatus())
                )

                // 고수 프로필의 mainLocation 기준 검색
                .filter(entry ->
                        locationId == null ||
                                (
                                        entry.getKey().getMainLocation() != null &&
                                                entry.getKey().getMainLocation()
                                                        .getLocationId()
                                                        .equals(locationId)
                                )
                )

                // 고수가 가진 category 기준 검색
                .filter(entry ->
                        categoryId == null ||
                                expertServices.stream()
                                        .filter(expertService ->
                                                expertService.getExpertProfile()
                                                        .getExpertProfileId()
                                                        .equals(entry.getKey().getExpertProfileId())
                                        )
                                        .anyMatch(expertService ->
                                                expertService.getCategory() != null &&
                                                        expertService.getCategory()
                                                                .getCategoryId()
                                                                .equals(categoryId)
                                        )
                )

                // keyword는 displayName 기준
                .filter(entry ->
                        keyword == null ||
                                keyword.isBlank() ||
                                (
                                        entry.getKey().getDisplayName() != null &&
                                                entry.getKey().getDisplayName().contains(keyword)
                                )
                )

                .map(entry ->
                        ExpertSearchResponse.from(
                                entry.getKey(),
                                entry.getValue()
                        )
                )
                .toList();
    }

    @Transactional(readOnly = true)
    public ExpertDetailResponse getExpertDetail(Long expertProfileId) {
        ExpertProfile expertProfile =
                expertProfileRepository.findById(expertProfileId)
                        .orElseThrow(() ->
                                new IllegalArgumentException("고수 프로필이 존재하지 않습니다.")
                        );

        if (!"ACTIVE".equals(expertProfile.getStatus()) ||
                expertProfile.getUser() == null ||
                "DELETED".equals(expertProfile.getUser().getStatus())) {
            throw new IllegalArgumentException("고수 프로필이 존재하지 않습니다.");
        }

        List<kyung.kung_backend.domain.servicepost.entity.ExpertService> expertServices =
                expertServiceRepository.findAllByExpertProfile(expertProfile);

        List<String> categoryNames =
                expertServices.stream()
                        .map(kyung.kung_backend.domain.servicepost.entity.ExpertService::getCategory)
                        .filter(Objects::nonNull)
                        .map(category -> category.getName())
                        .distinct()
                        .toList();

        List<ExpertDetailResponse.ServiceInfo> services =
                expertServices.stream()
                        .map(service ->
                                new ExpertDetailResponse.ServiceInfo(
                                        service.getCategory().getCategoryId(),
                                        service.getCategory().getName()
                                )
                        )
                        .toList();

        String webViewUrl = null;
        String externalUrl = expertProfile.getExternalPortfolioUrl();

        if (externalUrl != null && !externalUrl.isBlank()) {
            webViewUrl = "http://localhost:8080/api/portfolios/viewer?url=" + externalUrl;
        }

        return ExpertDetailResponse.from(
                expertProfile,
                categoryNames,
                services,
                webViewUrl
        );
    }

    private void updateExpertProfileAndServiceMapping(
            ExpertProfile expertProfile,
            String displayName,
            String introduction,
            Double careerYears,
            ServiceCategory mainCategory,
            Location mainLocation,
            String externalPortfolioUrl,
            List<Long> categoryIds
    ) {
        log.info("[mapping-update] start profileId={}", expertProfile.getExpertProfileId());

        expertProfile.updateProfile(
                displayName,
                introduction,
                careerYears,
                mainCategory,
                mainLocation,
                externalPortfolioUrl
        );

        log.info("[mapping-update] profile updated profileId={}", expertProfile.getExpertProfileId());

        List<kyung.kung_backend.domain.servicepost.entity.ExpertService> existingMappings =
                expertServiceRepository.findAllByExpertProfile(expertProfile);

        log.info("[mapping-update] existingMappings size={}", existingMappings.size());

        if (!existingMappings.isEmpty()) {
            expertServiceRepository.deleteAll(existingMappings);
            expertServiceRepository.flush();
        } else {
            log.info("[mapping-update] no old mappings, skip delete and flush");
        }

        saveExpertServiceMappings(
                expertProfile,
                mainCategory,
                categoryIds
        );

        log.info("[mapping-update] done profileId={}", expertProfile.getExpertProfileId());
    }

    private void saveExpertServiceMappings(
            ExpertProfile expertProfile,
            ServiceCategory mainCategory,
            List<Long> categoryIds
    ) {
        log.info("[mapping-save] start profileId={}, categoryIds={}",
                expertProfile.getExpertProfileId(),
                categoryIds
        );

        List<Long> serviceCategoryIds =
                categoryIds == null || categoryIds.isEmpty()
                        ? List.of(mainCategory.getCategoryId())
                        : new java.util.ArrayList<>(categoryIds);

        if (!serviceCategoryIds.contains(mainCategory.getCategoryId())) {
            serviceCategoryIds.add(mainCategory.getCategoryId());
        }

        List<Long> distinctCategoryIds =
                serviceCategoryIds.stream()
                        .distinct()
                        .toList();

        log.info("[mapping-save] distinctCategoryIds={}", distinctCategoryIds);

        List<ServiceCategory> categories =
                serviceCategoryRepository.findAllById(distinctCategoryIds);

        log.info("[mapping-save] loaded categories size={}", categories.size());

        if (categories.size() != distinctCategoryIds.size()) {
            throw new IllegalArgumentException("존재하지 않는 카테고리가 포함되어 있습니다.");
        }

        List<kyung.kung_backend.domain.servicepost.entity.ExpertService> mappings =
                categories.stream()
                        .map(category ->
                                kyung.kung_backend.domain.servicepost.entity.ExpertService.create(
                                        expertProfile,
                                        category
                                )
                        )
                        .toList();

        log.info("[mapping-save] mappings created size={}", mappings.size());

        expertServiceRepository.saveAll(mappings);
        expertServiceRepository.flush();

        log.info("[mapping-save] mappings saved and flushed");
    }
}