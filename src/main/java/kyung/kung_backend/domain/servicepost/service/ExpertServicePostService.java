package kyung.kung_backend.domain.servicepost.service;

import kyung.kung_backend.domain.category.entity.ServiceCategory;
import kyung.kung_backend.domain.category.repository.ServiceCategoryRepository;

import kyung.kung_backend.domain.expert.entity.ExpertProfile;
import kyung.kung_backend.domain.expert.repository.ExpertProfileRepository;

import kyung.kung_backend.domain.location.entity.Location;
import kyung.kung_backend.domain.location.repository.LocationRepository;

import kyung.kung_backend.domain.servicepost.dto.ExpertServiceCreateRequest;
import kyung.kung_backend.domain.servicepost.dto.ExpertServiceResponse;

import kyung.kung_backend.domain.servicepost.entity.ExpertService;
import kyung.kung_backend.domain.servicepost.repository.ExpertServiceRepository;

import kyung.kung_backend.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpertServicePostService {

    private final ExpertServiceRepository expertServiceRepository;

    private final ExpertProfileRepository expertProfileRepository;

    private final ServiceCategoryRepository serviceCategoryRepository;

    // =========================
    // 지역 Repository 추가
    // =========================

    private final LocationRepository locationRepository;

    public ExpertServiceResponse createExpertService(
            User user,
            ExpertServiceCreateRequest request
    ) {

        // =========================
        // 현재 로그인 유저의 고수 프로필 조회
        // =========================

        ExpertProfile expertProfile =
                expertProfileRepository
                        .findByUser_UserId(user.getUserId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "고수 프로필이 존재하지 않습니다."
                                )
                        );

        // =========================
        // 카테고리 조회
        // =========================

        ServiceCategory category =
                serviceCategoryRepository
                        .findById(request.getCategoryId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "카테고리가 존재하지 않습니다."
                                )
                        );

        // =========================
        // 지역 조회
        // =========================

        Location location =
                locationRepository
                        .findById(request.getLocationId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "지역이 존재하지 않습니다."
                                )
                        );

        // =========================
        // 중복 서비스 등록 방지
        // =========================

        if (
                expertServiceRepository
                        .existsByExpertProfileAndCategory_CategoryIdAndStatus(
                                expertProfile,
                                category.getCategoryId(),
                                "ACTIVE"
                        )
        ) {

            throw new IllegalArgumentException(
                    "이미 등록된 고수 서비스입니다."
            );
        }

        // =========================
        // 서비스 생성
        // =========================

        ExpertService expertService =
                ExpertService.create(
                        expertProfile,
                        category,
                        location,
                        request.getServiceTitle(),
                        request.getServiceDescription(),
                        request.getPrice()
                );

        ExpertService savedExpertService =
                expertServiceRepository.save(expertService);

        return ExpertServiceResponse.from(
                savedExpertService
        );
    }

    @Transactional(readOnly = true)
    public ExpertServiceResponse getExpertServiceDetail(
            Long serviceId
    ) {

        ExpertService expertService =
                expertServiceRepository
                        .findById(serviceId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "고수 서비스가 존재하지 않습니다."
                                )
                        );

        if (!expertService.isActive()) {

            throw new IllegalArgumentException(
                    "비활성화된 고수 서비스입니다."
            );
        }

        return ExpertServiceResponse.from(
                expertService
        );
    }
}