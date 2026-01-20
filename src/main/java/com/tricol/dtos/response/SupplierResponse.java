package com.tricol.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierResponse {
    private Long id;
    private String companyName;
    private String address;
    private String city;
    private String contactPerson;
    private String contactPhone;
    private String email;
    private String ice;
    private LocalDateTime createdAt;
}
