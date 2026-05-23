package kyung.kung_backend.domain.request.service;

import kyung.kung_backend.domain.chat.entity.ChatRoom;
import kyung.kung_backend.domain.chat.repository.ChatMessageRepository;
import kyung.kung_backend.domain.chat.repository.ChatRoomRepository;

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

    private final ServiceRequestRepository
            serviceRequestRepository;

    private final ExpertServiceRepository
            expertServiceRepository;

    private final UserRepository
            userRepository;

    private final ChatRoomRepository
            chatRoomRepository;

    // =========================
    // 추가
    // =========================
    private final ChatMessageRepository
            chatMessageRepository;

    // =========================
    // 견적 요청 생성
    // =========================
    @Transactional
    public ServiceRequestResponse createServiceRequest(
            User loginUser,
            ServiceRequestCreateRequest request
    ) {

        User user = findLoginUser(loginUser);

        ExpertService expertService =
                findExpertService(
                        request.getExpertServiceId()
                );

        ServiceRequest serviceRequest =
                ServiceRequest.create(
                        user,
                        expertService,
                        request.getTitle(),
                        request.getContent(),
                        request.getBudget(),
                        request.getPreferredDate()
                );

        ServiceRequest savedServiceRequest =
                serviceRequestRepository.save(
                        serviceRequest
                );

        return ServiceRequestResponse.from(
                savedServiceRequest
        );
    }

    // =========================
    // 내가 보낸 요청 조회
    // =========================
    public List<ServiceRequestResponse> getMyServiceRequests(
            User loginUser
    ) {

        User user = findLoginUser(loginUser);

        return serviceRequestRepository
                .findAllByUserUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(
                        user.getUserId()
                )
                .stream()
                .map(request -> {

                    Long chatRoomId =
                            chatRoomRepository
                                    .findByServiceRequest_RequestId(
                                            request.getRequestId()
                                    )
                                    .map(ChatRoom::getChatRoomId)
                                    .orElse(null);

                    // =========================
                    // unreadCount 계산
                    // =========================
                    Long unreadCount = 0L;

                    if (chatRoomId != null) {

                        unreadCount =

                                chatMessageRepository
                                        .countUnread(

                                                chatRoomId,

                                                user.getUserId()
                                        );
                    }

                    return ServiceRequestResponse.from(

                            request,

                            chatRoomId,

                            unreadCount
                    );
                })
                .toList();
    }

    // =========================
    // 받은 요청 조회
    // =========================
    public List<ServiceRequestResponse> getReceivedServiceRequests(
            User loginUser
    ) {

        User user = findLoginUser(loginUser);

        validateExpert(user);

        return serviceRequestRepository
                .findAllReceivedByExpertUserId(
                        user.getUserId()
                )
                .stream()
                .map(request -> {

                    Long chatRoomId =
                            chatRoomRepository
                                    .findByServiceRequest_RequestId(
                                            request.getRequestId()
                                    )
                                    .map(ChatRoom::getChatRoomId)
                                    .orElse(null);

                    // =========================
                    // unreadCount 계산
                    // =========================
                    Long unreadCount = 0L;

                    if (chatRoomId != null) {

                        unreadCount =

                                chatMessageRepository
                                        .countUnread(

                                                chatRoomId,

                                                user.getUserId()
                                        );
                    }

                    return ServiceRequestResponse.from(

                            request,

                            chatRoomId,

                            unreadCount
                    );
                })
                .toList();
    }

    // =========================
    // 요청 상세 조회
    // =========================
    public ServiceRequestResponse getServiceRequestDetail(
            User loginUser,
            Long requestId
    ) {

        User user = findLoginUser(loginUser);

        ServiceRequest serviceRequest =
                findServiceRequest(requestId);

        validateOwnerOrAdmin(
                user,
                serviceRequest
        );

        Long chatRoomId =
                chatRoomRepository
                        .findByServiceRequest_RequestId(
                                serviceRequest.getRequestId()
                        )
                        .map(ChatRoom::getChatRoomId)
                        .orElse(null);

        // =========================
        // unreadCount 계산
        // =========================
        Long unreadCount = 0L;

        if (chatRoomId != null) {

            unreadCount =

                    chatMessageRepository
                            .countUnread(

                                    chatRoomId,

                                    user.getUserId()
                            );
        }

        return ServiceRequestResponse.from(

                serviceRequest,

                chatRoomId,

                unreadCount
        );
    }

    // =========================
    // 요청 수정
    // =========================
    @Transactional
    public ServiceRequestResponse updateServiceRequest(
            User loginUser,
            Long requestId,
            ServiceRequestUpdateRequest request
    ) {

        User user = findLoginUser(loginUser);

        ServiceRequest serviceRequest =
                findServiceRequest(requestId);

        validateOwner(
                user,
                serviceRequest
        );

        validateUpdatable(serviceRequest);

        serviceRequest.update(
                request.getTitle(),
                request.getContent(),
                request.getBudget(),
                request.getPreferredDate()
        );

        Long chatRoomId =
                chatRoomRepository
                        .findByServiceRequest_RequestId(
                                serviceRequest.getRequestId()
                        )
                        .map(ChatRoom::getChatRoomId)
                        .orElse(null);

        return ServiceRequestResponse.from(
                serviceRequest,
                chatRoomId
        );
    }

    // =========================
    // 요청 취소
    // =========================
    @Transactional
    public ServiceRequestResponse cancelServiceRequest(
            User loginUser,
            Long requestId
    ) {

        User user = findLoginUser(loginUser);

        ServiceRequest serviceRequest =
                findServiceRequest(requestId);

        validateOwner(
                user,
                serviceRequest
        );

        validateCancelable(serviceRequest);

        serviceRequest.cancel();

        Long chatRoomId =
                chatRoomRepository
                        .findByServiceRequest_RequestId(
                                serviceRequest.getRequestId()
                        )
                        .map(ChatRoom::getChatRoomId)
                        .orElse(null);

        return ServiceRequestResponse.from(
                serviceRequest,
                chatRoomId
        );
    }

    // =========================
    // 요청 승인
    // =========================
    @Transactional
    public ServiceRequestResponse approveServiceRequest(
            User loginUser,
            Long requestId
    ) {

        User user = findLoginUser(loginUser);

        ServiceRequest serviceRequest =
                findServiceRequest(requestId);

        validateExpert(user);

        Long requestExpertUserId =
                serviceRequest.getExpertService()
                        .getExpertProfile()
                        .getUser()
                        .getUserId();

        if (
                !requestExpertUserId.equals(
                        user.getUserId()
                )
        ) {

            throw GeneralException.of(
                    ErrorCode.FORBIDDEN
            );
        }

        validateApprovable(serviceRequest);

        serviceRequest.startChatting();

        if (
                serviceRequest.getExpertService() == null
                        ||
                        serviceRequest.getExpertService()
                                .getExpertProfile() == null
        ) {

            throw new IllegalArgumentException(
                    "고수 프로필이 존재하지 않습니다."
            );
        }

        ChatRoom chatRoom = ChatRoom.create(

                serviceRequest,

                serviceRequest.getUser(),

                serviceRequest.getExpertService()
                        .getExpertProfile()
        );

        ChatRoom savedChatRoom =
                chatRoomRepository.save(
                        chatRoom
                );

        return ServiceRequestResponse.from(
                serviceRequest,
                savedChatRoom.getChatRoomId()
        );
    }

    // =========================
    // 요청 거절
    // =========================
    @Transactional
    public ServiceRequestResponse rejectServiceRequest(
            User loginUser,
            Long requestId
    ) {

        User user = findLoginUser(loginUser);

        ServiceRequest serviceRequest =
                findServiceRequest(requestId);

        validateExpert(user);

        validateRejectable(serviceRequest);

        serviceRequest.reject(null);

        return ServiceRequestResponse.from(
                serviceRequest
        );
    }

    private User findLoginUser(User loginUser) {

        if (
                loginUser == null
                        ||
                        loginUser.getUserId() == null
        ) {

            throw GeneralException.of(
                    ErrorCode.UNAUTHORIZED
            );
        }

        return userRepository.findById(
                        loginUser.getUserId()
                )
                .orElseThrow(() ->
                        GeneralException.of(
                                ErrorCode.UNAUTHORIZED
                        )
                );
    }

    private ExpertService findExpertService(
            Long expertServiceId
    ) {

        return expertServiceRepository.findById(
                        expertServiceId
                )
                .orElseThrow(() ->
                        GeneralException.of(
                                ErrorCode.NOT_FOUND
                        )
                );
    }

    private ServiceRequest findServiceRequest(
            Long requestId
    ) {

        return serviceRequestRepository
                .findByRequestIdAndDeletedAtIsNull(
                        requestId
                )
                .orElseThrow(() ->
                        GeneralException.of(
                                ErrorCode.NOT_FOUND
                        )
                );
    }

    private void validateOwner(
            User user,
            ServiceRequest serviceRequest
    ) {

        if (
                !serviceRequest.getUser()
                        .getUserId()
                        .equals(user.getUserId())
        ) {

            throw GeneralException.of(
                    ErrorCode.FORBIDDEN
            );
        }
    }

    private void validateOwnerOrAdmin(
            User user,
            ServiceRequest serviceRequest
    ) {

        if (isAdmin(user)) {
            return;
        }

        boolean isRequester =
                serviceRequest.getUser()
                        .getUserId()
                        .equals(user.getUserId());

        boolean isExpert =
                serviceRequest.getExpertService()
                        .getExpertProfile()
                        .getUser()
                        .getUserId()
                        .equals(user.getUserId());

        if (!isRequester && !isExpert) {

            throw GeneralException.of(
                    ErrorCode.FORBIDDEN
            );
        }
    }

    private boolean isAdmin(User user) {

        return ROLE_ADMIN.equals(
                user.getRole()
        );
    }

    private void validateUpdatable(
            ServiceRequest serviceRequest
    ) {

        if (!serviceRequest.isPending()) {

            throw GeneralException.of(
                    ErrorCode.BAD_REQUEST
            );
        }
    }

    private void validateCancelable(
            ServiceRequest serviceRequest
    ) {

        if (!serviceRequest.isPending()) {

            throw GeneralException.of(
                    ErrorCode.BAD_REQUEST
            );
        }
    }

    private void validateApprovable(
            ServiceRequest serviceRequest
    ) {

        if (!serviceRequest.isPending()) {

            throw GeneralException.of(
                    ErrorCode.BAD_REQUEST
            );
        }
    }

    private void validateRejectable(
            ServiceRequest serviceRequest
    ) {

        if (!serviceRequest.isPending()) {

            throw GeneralException.of(
                    ErrorCode.BAD_REQUEST
            );
        }
    }

    private void validateExpert(User user) {

        if (
                !"EXPERT".equals(
                        user.getRole()
                )
        ) {

            throw GeneralException.of(
                    ErrorCode.FORBIDDEN
            );
        }
    }
}