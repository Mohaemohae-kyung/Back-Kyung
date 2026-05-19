package kyung.kung_backend.domain.payment.dto;

import kyung.kung_backend.domain.expert.entity.ExpertProfile;
import kyung.kung_backend.domain.request.entity.ServiceRequest;
import kyung.kung_backend.domain.request.enums.RequestStatus;
import kyung.kung_backend.domain.servicepost.entity.ExpertService;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class ServiceRequestCheckoutResponse {

    private Long requestId;
    private Long expertServiceId;
    private Long expertProfileId;
    private String serviceTitle;
    private String expertDisplayName;
    private LocalDateTime preferredDate;
    private RequestStatus requestStatus;
    private BigDecimal baseAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;

    /*
     * ServiceRequest 기반 결제 화면 응답입니다.
     * 사용자가 고수에게 요청한 희망 일시와 예산을 결제 화면에 보여주고,
     * 실제 결제 준비 API에서는 같은 requestId를 다시 조회해서 금액을 재검증합니다.
     */
    public static ServiceRequestCheckoutResponse of(
            ServiceRequest serviceRequest,
            BigDecimal baseAmount,
            BigDecimal discountAmount,
            BigDecimal finalAmount
    ) {
        ExpertService expertService = serviceRequest.getExpertService();
        ExpertProfile expertProfile = expertService != null ? expertService.getExpertProfile() : null;

        return ServiceRequestCheckoutResponse.builder()
                .requestId(serviceRequest.getRequestId())
                .expertServiceId(expertService != null ? expertService.getExpertServiceId() : null)
                .expertProfileId(expertProfile != null ? expertProfile.getExpertProfileId() : null)
                .serviceTitle(serviceRequest.getTitle())
                .expertDisplayName(expertProfile != null ? expertProfile.getDisplayName() : null)
                .preferredDate(serviceRequest.getPreferredDate())
                .requestStatus(serviceRequest.getStatus())
                .baseAmount(baseAmount)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .build();
    }
}
