package kyung.kung_backend.domain.request.dto;

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
    private Long categoryId;
    private Long locationId;
    private Long expertServiceId;
    private String title;
    private String content;
    private BigDecimal budget;
    private LocalDateTime preferredDate;
    private RequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ServiceRequestResponse from(ServiceRequest serviceRequest) {
        return ServiceRequestResponse.builder()
                .requestId(serviceRequest.getRequestId())
                .userId(serviceRequest.getUser().getUserId())
                .categoryId(serviceRequest.getCategory().getCategoryId())
                .locationId(serviceRequest.getLocation() != null
                        ? serviceRequest.getLocation().getLocationId()
                        : null)
                .expertServiceId(serviceRequest.getExpertService() != null
                        ? serviceRequest.getExpertService().getExpertServiceId()
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