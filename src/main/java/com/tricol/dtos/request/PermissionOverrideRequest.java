package com.tricol.dtos.request;

import com.tricol.entities.enums.PermissionAction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PermissionOverrideRequest {
    @NotBlank
    private String permissionName;
    
    @NotNull
    private PermissionAction action; 
}
