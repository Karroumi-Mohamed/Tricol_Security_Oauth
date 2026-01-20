package com.tricol.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class StockValuationResponse {
    private BigDecimal totalValue;
    private int totalProducts;
    private int totalLots;
    private LocalDateTime calculatedAt;
    private List<ProductStockResponse> products;
}
