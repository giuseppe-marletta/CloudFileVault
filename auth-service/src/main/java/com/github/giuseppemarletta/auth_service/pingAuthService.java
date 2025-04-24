package com.github.giuseppemarletta.auth_service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class pingAuthService {

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}

