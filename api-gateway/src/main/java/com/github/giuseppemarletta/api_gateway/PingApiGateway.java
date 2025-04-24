package com.github.giuseppemarletta.api_gateway;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingApiGateway {
    
    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
    
}
