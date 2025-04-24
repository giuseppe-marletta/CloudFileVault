package com.github.giuseppemarletta.auth_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class AuthServiceApplication {
   public AuthServiceApplication() {
   }

   public static void main(String[] args) {
      SpringApplication.run(AuthServiceApplication.class, args);
      System.out.println("Auth Service is running...");
   }

   @GetMapping({"/hello"})
   public String hello() {
      return "Hello from Auth Service!";
   }
}

