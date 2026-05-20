package kyung.kung_backend.domain.request.service;

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

    private static final String ROLE_ADMIN = "ADMIN";

    private final ServiceRequestRepository serviceRequestRepository;
    private final ExpertServiceRepository expertServiceRepository;
    private final UserRepository userRepository;

    @Transactional
    public ServiceRequestResponse createServiceRequest(
            User loginUser,
            ServiceRequestCreateRequest request
    ) {
        User user = findLoginUser(loginUser);
        ExpertService expertService = findExpertService(request.getExpertServiceId());

        ServiceRequest serviceRequest = ServiceRequest.create(
                user,
                expertService,
                request.getTitle(),
                request.getContent(),
                request.getBudget(),
                request.getPreferredDate()
        );

        ServiceRequest savedServiceRequest = serviceRequestRepository.save(serviceRequest);

        return ServiceRequestResponse.from(savedServiceRequest);
    }

    public List<ServiceRequestResponse> getMyServiceRequests(User loginUser) {
        User user = findLoginUser(loginUser);

        return serviceRequestRepository.findAllByUserUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(user.getUserId())
                .stream()
                .map(ServiceRequestResponse::from)
                .toList();
    }

    public List<ServiceRequestResponse> getReceivedServiceRequests(User loginUser) {
        User user = findLoginUser(loginUser);

        validateExpert(user);

        return serviceRequestRepository.findAllReceivedByExpertUserId(user.getUserId())
                .stream()
                .map(ServiceRequestResponse::from)
                .toList();
    }

    public ServiceRequestResponse getServiceRequestDetail(
            User loginUser,
            Long requestId
    ) {
        User user = findLoginUser(loginUser);
        ServiceRequest serviceRequest = findServiceRequest(requestId);

        validateOwnerOrAdmin(user, serviceRequest);

        return ServiceRequestResponse.from(serviceRequest);
    }

    @Transactional
    public ServiceRequestResponse updateServiceRequest(
            User loginUser,
            Long requestId,
            ServiceRequestUpdateRequest request
    ) {
        User user = findLoginUser(loginUser);
        ServiceRequest serviceRequest = findServiceRequest(requestId);

        validateOwner(user, serviceRequest);
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
            User loginUser,
            Long requestId
    ) {
        User user = findLoginUser(loginUser);
        ServiceRequest serviceRequest = findServiceRequest(requestId);

        validateOwner(user, serviceRequest);
        validateCancelable(serviceRequest);

        serviceRequest.cancel();

        return ServiceRequestResponse.from(serviceRequest);
    }

    @Transactional
    public ServiceRequestResponse approveServiceRequest(
            User loginUser,
            Long requestId
    ) {
        User user = findLoginUser(loginUser);
        ServiceRequest serviceRequest = findServiceRequest(requestId);

        validateExpert(user);
        validateApprovable(serviceRequest);

        serviceRequest.startChatting();

        // TODO: ChatRoom 생성 로직 추가 예정
        // chatRoomService.createByServiceRequest(serviceRequest);

        return ServiceRequestResponse.from(serviceRequest);
    }

    @Transactional
    public ServiceRequestResponse rejectServiceRequest(
            User loginUser,
            Long requestId
    ) {
        User user = findLoginUser(loginUser);
        ServiceRequest serviceRequest = findServiceRequest(requestId);

        validateExpert(user);
        validateRejectable(serviceRequest);

        serviceRequest.reject(null);

        return ServiceRequestResponse.from(serviceRequest);
    }

    private User findLoginUser(User loginUser) {
        if (loginUser == null || loginUser.getUserId() == null) {
            throw GeneralException.of(ErrorCode.UNAUTHORIZED);
        }

        return userRepository.findById(loginUser.getUserId())
                .orElseThrow(() -> GeneralException.of(ErrorCode.UNAUTHORIZED));
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
            User user,
            ServiceRequest serviceRequest
    ) {
        if (!serviceRequest.getUser().getUserId().equals(user.getUserId())) {
            throw GeneralException.of(ErrorCode.FORBIDDEN);
        }
    }

    private void validateOwnerOrAdmin(
            User user,
            ServiceRequest serviceRequest
    ) {
        if (isAdmin(user)) {
            return;
        }

        validateOwner(user, serviceRequest);
    }

    private boolean isAdmin(User user) {
        return ROLE_ADMIN.equals(user.getRole());
    }

    private void validateUpdatable(ServiceRequest serviceRequest) {
        if (!serviceRequest.isPending()) {
            throw GeneralException.of(ErrorCode.BAD_REQUEST);
        }
    }

    private void validateCancelable(ServiceRequest serviceRequest) {
        if (!serviceRequest.isPending()) {
            throw GeneralException.of(ErrorCode.BAD_REQUEST);
        }
    }

    private void validateApprovable(ServiceRequest serviceRequest) {
        if (!serviceRequest.isPending()) {
            throw GeneralException.of(ErrorCode.BAD_REQUEST);
        }
    }

    private void validateRejectable(ServiceRequest serviceRequest) {
        if (!serviceRequest.isPending()) {
            throw GeneralException.of(ErrorCode.BAD_REQUEST);
        }
    }

    private void validateExpert(User user) {
        if (!"EXPERT".equals(user.getRole())) {
            throw GeneralException.of(ErrorCode.FORBIDDEN);
        }
    }
}