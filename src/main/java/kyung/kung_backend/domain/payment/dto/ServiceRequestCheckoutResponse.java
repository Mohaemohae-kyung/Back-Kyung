package kyung.kung_backend.domain.payment.dto;

import kyung.kung_backend.domain.category.entity.ServiceCategory;
import kyung.kung_backend.domain.expert.entity.ExpertProfile;
import kyung.kung_backend.domain.request.entity.ServiceRequest;
import kyung.kung_backend.domain.request.enums.RequestStatus;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class ServiceRequestCheckoutResponse {

    private Long requestId;

    private Long expertProfileId;
    private String expertDisplayName;

    private Long categoryId;
    private String categoryName;

    private String requestTitle;

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
        ExpertProfile expertProfile =
                serviceRequest.getExpertProfile();

        ServiceCategory category =
                serviceRequest.getCategory();

        return ServiceRequestCheckoutResponse.builder()
                .requestId(serviceRequest.getRequestId())
                .expertProfileId(
                        expertProfile != null
                                ? expertProfile.getExpertProfileId()
                                : null
                )
                .expertDisplayName(
                        expertProfile != null
                                ? expertProfile.getDisplayName()
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
                .requestTitle(serviceRequest.getTitle())
                .preferredDate(serviceRequest.getPreferredDate())
                .requestStatus(serviceRequest.getStatus())
                .baseAmount(baseAmount)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .build();
    }
}