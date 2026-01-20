package com.tricol.dtos.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {
    @NotBlank(message = "Reference is required")
    private String reference;

    @NotBlank(message = "Name is required")
    private String name;

    private String Description;

    @DecimalMin(value = "0.0", message = "Unit price must be positive")
    private BigDecimal unitPrice;

    private String category;

    @DecimalMin(value = "0.0", message = "Current stock must be positive")
    private BigDecimal reorderLevel;

    private String unitOfMeasure;
}
