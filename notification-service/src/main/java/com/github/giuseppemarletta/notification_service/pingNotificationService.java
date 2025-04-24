package com.github.giuseppemarletta.notification_service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class pingNotificationService {

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}
