package com.davidparker.dms.admin.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @Email(message = "Email must be valid")
    private String email;
    
    private String displayName;
}
