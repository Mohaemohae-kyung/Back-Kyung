package kyung.kung_backend.domain.request.dto;

import kyung.kung_backend.domain.category.entity.ServiceCategory;
import kyung.kung_backend.domain.expert.entity.ExpertProfile;
import kyung.kung_backend.domain.location.entity.Location;
import kyung.kung_backend.domain.request.entity.ServiceRequest;
import kyung.kung_backend.domain.request.enums.RequestStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequestResponse {

    private Long requestId;

    private Long userId;

    private Long expertProfileId;

    private Long categoryId;

    private String categoryName;

    private Long locationId;

    // =========================
    // 채팅방 ID
    // =========================
    private Long chatRoomId;

    // =========================
    // 안 읽은 메시지 수
    // =========================
    private Long unreadCount;

    // =========================
    // 요청자 닉네임
    // =========================
    private String requesterName;

    private String title;

    private String content;

    private BigDecimal budget;

    private LocalDateTime preferredDate;

    private RequestStatus status;

    private String paymentMode;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static ServiceRequestResponse from(
            ServiceRequest serviceRequest
    ) {

        return from(
                serviceRequest,
                null,
                0L
        );
    }

    public static ServiceRequestResponse from(
            ServiceRequest serviceRequest,
            Long chatRoomId
    ) {

        return from(
                serviceRequest,
                chatRoomId,
                0L
        );
    }

    public static ServiceRequestResponse from(
            ServiceRequest serviceRequest,
            Long chatRoomId,
            Long unreadCount
    ) {

        ExpertProfile expertProfile =
                serviceRequest.getExpertProfile();

        ServiceCategory category =
                serviceRequest.getCategory();

        Location mainLocation = null;

        if (expertProfile != null) {

            mainLocation =
                    expertProfile.getMainLocation();
        }

        return ServiceRequestResponse.builder()

                .requestId(
                        serviceRequest.getRequestId()
                )

                .userId(
                        serviceRequest
                                .getUser()
                                .getUserId()
                )

                .expertProfileId(
                        expertProfile != null
                                ? expertProfile.getExpertProfileId()
                                : null
                )

                .categoryId(
                        category != null
                                ? category.getCategoryId()
                                : null
                )

                .categoryName(
                        category != null
                                ? category.getName()
                                : null
                )

                .locationId(
                        mainLocation != null
                                ? mainLocation.getLocationId()
                                : null
                )

                .chatRoomId(
                        chatRoomId
                )

                .unreadCount(
                        unreadCount
                )

                .requesterName(
                        serviceRequest
                                .getUser()
                                .getNickname()
                )

                .title(
                        serviceRequest.getTitle()
                )

                .content(
                        serviceRequest.getContent()
                )

                .budget(
                        serviceRequest.getBudget()
                )

                .preferredDate(
                        serviceRequest.getPreferredDate()
                )

                .status(
                        serviceRequest.getStatus()
                )

                .paymentMode(
                        serviceRequest.getPaymentMode()
                )

                .createdAt(
                        serviceRequest.getCreatedAt()
                )

                .updatedAt(
                        serviceRequest.getUpdatedAt()
                )

                .build();
    }
}