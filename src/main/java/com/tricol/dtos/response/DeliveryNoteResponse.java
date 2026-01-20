package com.tricol.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryNoteResponse {
    private Long id;
    private String deliveryNoteNumber;
    private String workshop;
    private LocalDate exitDate;
    private String exitReason;
    private List<DeliveryNoteLineResponse> lines;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime validatedAt;
}
