package kyung.kung_backend.domain.request.service;

import kyung.kung_backend.domain.category.entity.ServiceCategory;
import kyung.kung_backend.domain.location.entity.Location;
import kyung.kung_backend.domain.request.dto.ServiceRequestCreateRequest;
import kyung.kung_backend.domain.request.dto.ServiceRequestResponse;
import kyung.kung_backend.domain.request.dto.ServiceRequestUpdateRequest;
import kyung.kung_backend.domain.request.entity.ServiceRequest;
import kyung.kung_backend.domain.request.repository.ServiceRequestRepository;
import kyung.kung_backend.domain.servicepost.entity.ExpertService;
import kyung.kung_backend.domain.servicepost.repository.ExpertServiceRepository;
import kyung.kung_backend.domain.user.entity.User;
import kyung.kung_backend.domain.user.repository.UserRepository;
import kyung.kung_backend.global.exception.GeneralException;
import kyung.kung_backend.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final UserRepository userRepository;
    private final ExpertServiceRepository expertServiceRepository;

    @Transactional
    public ServiceRequestResponse createServiceRequest(
            Long userId,
            ServiceRequestCreateRequest request
    ) {
        User user = findUser(userId);
        ExpertService expertService = findExpertService(request.getExpertServiceId());

        ServiceCategory category = expertService.getCategory();
        Location location = expertService.getLocation();

        ServiceRequest serviceRequest = ServiceRequest.create(
                user,
                category,
                location,
                expertService,
                request.getTitle(),
                request.getContent(),
                request.getBudget(),
                request.getPreferredDate()
        );

        ServiceRequest savedServiceRequest = serviceRequestRepository.save(serviceRequest);

        return ServiceRequestResponse.from(savedServiceRequest);
    }

    public List<ServiceRequestResponse> getMyServiceRequests(Long userId) {
        findUser(userId);

        return serviceRequestRepository.findAllByUserUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId)
                .stream()
                .map(ServiceRequestResponse::from)
                .toList();
    }

    public ServiceRequestResponse getServiceRequestDetail(
            Long userId,
            Long requestId
    ) {
        ServiceRequest serviceRequest = findServiceRequest(requestId);

        validateOwner(userId, serviceRequest);

        return ServiceRequestResponse.from(serviceRequest);
    }

    @Transactional
    public ServiceRequestResponse updateServiceRequest(
            Long userId,
            Long requestId,
            ServiceRequestUpdateRequest request
    ) {
        ServiceRequest serviceRequest = findServiceRequest(requestId);

        validateOwner(userId, serviceRequest);
        validateUpdatable(serviceRequest);

        serviceRequest.update(
                request.getTitle(),
                request.getContent(),
                request.getBudget(),
                request.getPreferredDate()
        );

        return ServiceRequestResponse.from(serviceRequest);
    }

    @Transactional
    public ServiceRequestResponse cancelServiceRequest(
            Long userId,
            Long requestId
    ) {
        ServiceRequest serviceRequest = findServiceRequest(requestId);

        validateOwner(userId, serviceRequest);
        validateCancelable(serviceRequest);

        serviceRequest.cancel();

        return ServiceRequestResponse.from(serviceRequest);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> GeneralException.of(ErrorCode.NOT_FOUND));
    }

    private ExpertService findExpertService(Long expertServiceId) {
        return expertServiceRepository.findById(expertServiceId)
                .orElseThrow(() -> GeneralException.of(ErrorCode.NOT_FOUND));
    }

    private ServiceRequest findServiceRequest(Long requestId) {
        return serviceRequestRepository.findByRequestIdAndDeletedAtIsNull(requestId)
                .orElseThrow(() -> GeneralException.of(ErrorCode.NOT_FOUND));
    }

    private void validateOwner(
            Long userId,
            ServiceRequest serviceRequest
    ) {
        if (!serviceRequest.getUser().getUserId().equals(userId)) {
            throw GeneralException.of(ErrorCode.FORBIDDEN);
        }
    }

    private void validateUpdatable(ServiceRequest serviceRequest) {
        if (serviceRequest.isCancelled()) {
            throw GeneralException.of(ErrorCode.BAD_REQUEST);
        }

        // TODO: 추후 매칭/예약/결제 진행 상태에 따라 수정 제한 정책 추가
    }

    private void validateCancelable(ServiceRequest serviceRequest) {
        if (serviceRequest.isCancelled()) {
            throw GeneralException.of(ErrorCode.BAD_REQUEST);
        }

        // TODO: 추후 매칭/예약/결제 진행 상태에 따라 취소 제한 정책 추가
    }
}