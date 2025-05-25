package com.github.giuseppemarletta.auth_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.giuseppemarletta.auth_service.security.annotation.RequireRole;
import com.github.giuseppemarletta.auth_service.Repository.UserRepository;
import com.github.giuseppemarletta.auth_service.model.User;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @RequireRole({"ADMIN", "MODERATOR"})
    @GetMapping
    public ResponseEntity<List<String>> getAllUsers() {
        System.out.println("Getting all users");
        List<String> users = List.of("admin@example.com", "mod@example.com", "user@example.com");  //mock data
        return ResponseEntity.ok(users);
    }

    @RequireRole({"ADMIN"})
    @DeleteMapping("/{email}")
    public ResponseEntity<Void> deleteUser(@PathVariable String email) {
        System.out.println("Deleting user: " + email);
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
            
        userRepository.delete(user);
        System.out.println("User deleted successfully: " + email);
        
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/public")
    public ResponseEntity<String> publicEndpoint() {
        return ResponseEntity.ok("This is a public endpoint");
    }

}
