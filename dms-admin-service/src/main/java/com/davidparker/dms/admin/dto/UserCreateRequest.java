package com.davidparker.dms.admin.dto;

import lombok.Data;

@Data
public class UserCreateRequest {
    private String username;
    private String email;
    private String displayName;
}
