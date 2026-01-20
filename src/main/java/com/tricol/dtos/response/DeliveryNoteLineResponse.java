package com.tricol.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryNoteLineResponse {
    private Long id;
    private Long productId;
    private String productReference;
    private String productName;
    private BigDecimal quantity;
    private String unitOfMeasure;
}
