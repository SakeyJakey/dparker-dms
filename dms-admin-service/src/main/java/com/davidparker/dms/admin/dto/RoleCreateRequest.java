package com.davidparker.dms.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleCreateRequest {
    @NotBlank(message = "Role name is required")
    private String name;
    private String description;
}
