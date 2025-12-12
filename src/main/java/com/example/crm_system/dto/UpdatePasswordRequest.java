package com.example.crm_system.dto;

import lombok.Data;
@Data

public class UpdatePasswordRequest {
     private String identifier;      // email or mobile (like login)
    private String currentPassword;
    private String newPassword;
}
