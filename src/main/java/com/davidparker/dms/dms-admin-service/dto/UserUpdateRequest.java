package com.davidparker.dms.dms_admin_service.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String email;
    private String displayName;
}
