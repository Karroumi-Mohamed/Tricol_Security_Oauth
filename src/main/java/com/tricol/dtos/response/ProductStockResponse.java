package com.tricol.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductStockResponse {
    private Long productId;
    private String productReference;
    private String productName;
    private String unitOfMeasure;
    private BigDecimal totalQuantity;
    private BigDecimal totalValue;
    private BigDecimal reorderLevel;
    private boolean belowReorderLevel;
    private List<StockLotResponse> lots; // FIFO ordered
}
