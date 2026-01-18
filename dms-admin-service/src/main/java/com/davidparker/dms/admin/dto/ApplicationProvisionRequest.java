package com.davidparker.dms.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApplicationProvisionRequest {
    @NotBlank(message = "Entra App ID is required")
    private String entraAppId;
    
    @NotBlank(message = "Application name is required")
    private String applicationName;
}
