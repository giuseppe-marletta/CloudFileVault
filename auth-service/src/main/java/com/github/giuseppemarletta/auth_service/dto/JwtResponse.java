package com.github.giuseppemarletta.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class JwtResponse {
    private String Token;
}
