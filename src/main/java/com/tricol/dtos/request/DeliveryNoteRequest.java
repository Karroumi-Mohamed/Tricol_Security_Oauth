package com.tricol.dtos.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryNoteRequest {

    @NotBlank(message = "Workshop is required")
    private String workshop;

    private LocalDate exitDate;

    private String exitReason;

    @NotEmpty(message = "Delivery note must contain at least one line item")
    @Valid
    private List<DeliveryNoteLineRequest> lines;
}
