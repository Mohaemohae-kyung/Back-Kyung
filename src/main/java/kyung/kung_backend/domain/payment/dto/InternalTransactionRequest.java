package kyung.kung_backend.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InternalTransactionRequest {
    private String orderId;
    private Long itemId;
    private BigDecimal finalAmount;
}
