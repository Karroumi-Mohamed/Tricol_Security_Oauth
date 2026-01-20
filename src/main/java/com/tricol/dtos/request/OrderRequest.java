package com.tricol.dtos.request;

import com.tricol.entities.OrderLine;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {
    @NotNull(message = "Supplier ID is required")
    private Long supplierId;


    private LocalDate orderDate;

    @NotEmpty(message = "Order must contain at least one line item")
    @Valid
    private List<OrderLineRequest> lines;
}
