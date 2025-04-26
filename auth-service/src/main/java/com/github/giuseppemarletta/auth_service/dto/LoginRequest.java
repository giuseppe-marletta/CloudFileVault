package com.github.giuseppemarletta.auth_service.dto;

import lombok.Data;


@Data
public class LoginRequest {
    private String Email;
    private String Password;
}
