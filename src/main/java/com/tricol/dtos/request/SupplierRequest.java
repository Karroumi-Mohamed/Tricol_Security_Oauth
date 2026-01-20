package com.tricol.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierRequest {
    @NotBlank(message = "Company name is required")
    private String companyName;

    private  String address;

    private String city;

    private String contactPerson;

    private String contactPhone;

    @Email(message = "Invalid email format")
    private String email;

    private String ice;
}
