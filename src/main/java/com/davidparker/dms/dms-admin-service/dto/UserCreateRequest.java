package com.davidparker.dms.dms_admin_service.dto;

import lombok.Data;

@Data
public class UserCreateRequest {
    private String username;
    private String email;
    private String displayName;
}
