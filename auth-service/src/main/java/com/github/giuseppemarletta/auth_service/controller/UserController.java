package com.github.giuseppemarletta.auth_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.giuseppemarletta.auth_service.security.annotation.RequireRole;

@RestController
@RequestMapping("/users")
public class UserController {

    @RequireRole({"ADMIN", "MODERATOR"})
    @GetMapping
    public ResponseEntity<List<String>> getAllUsers() {
        List<String> users = List.of("admin@example.com", "mod@example.com", "user@example.com");  //mock data
        return ResponseEntity.ok(users);
    }

    @RequireRole({"ADMIN"})
    @DeleteMapping("/{email}")
    public ResponseEntity<Void> deleteUser(@PathVariable String email) {
        System.out.println("Deleting user: " + email);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/public")
    public ResponseEntity<String> publicEndpoint() {
        return ResponseEntity.ok("This is a public endpoint");
    }

}
