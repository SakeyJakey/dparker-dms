package com.davidparker.dms.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PermissionCreateRequest {
    @NotBlank(message = "Permission name is required")
    private String name;
    private String description;
    private String resourceType;
}
