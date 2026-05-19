package kyung.kung_backend.domain.request.dto;

import kyung.kung_backend.domain.location.entity.Location;
import kyung.kung_backend.domain.request.entity.ServiceRequest;
import kyung.kung_backend.domain.request.enums.RequestStatus;
import kyung.kung_backend.domain.servicepost.entity.ExpertService;
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

    private Long expertServiceId;
    private Long expertProfileId;

    private Long categoryId;
    private Long locationId;

    private String title;
    private String content;
    private BigDecimal budget;
    private LocalDateTime preferredDate;
    private RequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ServiceRequestResponse from(ServiceRequest serviceRequest) {
        ExpertService expertService = serviceRequest.getExpertService();
        Location mainLocation = null;

        if (expertService != null && expertService.getExpertProfile() != null) {
            mainLocation = expertService.getExpertProfile().getMainLocation();
        }

        return ServiceRequestResponse.builder()
                .requestId(serviceRequest.getRequestId())
                .userId(serviceRequest.getUser().getUserId())

                .expertServiceId(expertService != null
                        ? expertService.getExpertServiceId()
                        : null)
                .expertProfileId(expertService != null && expertService.getExpertProfile() != null
                        ? expertService.getExpertProfile().getExpertProfileId()
                        : null)

                .categoryId(expertService != null && expertService.getCategory() != null
                        ? expertService.getCategory().getCategoryId()
                        : null)
                .locationId(mainLocation != null
                        ? mainLocation.getLocationId()
                        : null)

                .title(serviceRequest.getTitle())
                .content(serviceRequest.getContent())
                .budget(serviceRequest.getBudget())
                .preferredDate(serviceRequest.getPreferredDate())
                .status(serviceRequest.getStatus())
                .createdAt(serviceRequest.getCreatedAt())
                .updatedAt(serviceRequest.getUpdatedAt())
                .build();
    }
}