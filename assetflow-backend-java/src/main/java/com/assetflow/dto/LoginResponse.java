package com.assetflow.dto;

import com.assetflow.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {

    private boolean success;
    private String message;

    private Long userId;
    private String name;
    private String email;
    private UserRole role;
}