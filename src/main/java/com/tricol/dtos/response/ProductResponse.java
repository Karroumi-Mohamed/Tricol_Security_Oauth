package com.tricol.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private Long id;
    private String reference;
    private String name;
    private String description;
    private BigDecimal unitPrice;
    private String category;
    private BigDecimal currentStock;
    private BigDecimal reorderLevel;
    private String unitOfMeasure;
    private LocalDateTime createdAt;
    private boolean belowReorderLevel;
}
