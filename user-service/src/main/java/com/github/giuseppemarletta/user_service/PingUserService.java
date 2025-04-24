package com.github.giuseppemarletta.user_service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class PingUserService {
    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}
