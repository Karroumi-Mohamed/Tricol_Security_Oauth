package com.tricol.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockLotResponse {

    private Long id;
    private String lotNumber;
    private Long productId;
    private String productReference;
    private String productName;
    private Long orderId;
    private String orderNumber;
    private BigDecimal initialQuantity;
    private BigDecimal remainingQuantity;
    private BigDecimal purchasePrice;
    private BigDecimal lotValue;
    private LocalDateTime entryDate;
}
