package com.github.giuseppemarletta.auth_service.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String Email;
    private String Password;
    private String Role;  // USER, MODERATOR, ADMIN
}
