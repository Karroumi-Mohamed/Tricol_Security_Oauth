package com.tricol.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementResponse {
    private Long id;
    private Long productId;
    private String productReference;
    private String productName;
    private Long stockLotId;
    private String lotNumber;
    private String movementType;
    private BigDecimal quantity;
    private LocalDateTime movementDate;
    private String reference;
}
