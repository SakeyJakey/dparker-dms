package com.davidparker.dms.dms_admin_service.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class RoleCreateRequest {
    private String name;
    private String description;
    private List<UUID> permissionIds;
}
