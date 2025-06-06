package com.github.giuseppemarletta.auth_service.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.giuseppemarletta.auth_service.Repository.UserRepository;
import com.github.giuseppemarletta.auth_service.model.User;

import com.github.giuseppemarletta.auth_service.dto.JwtResponse;
import com.github.giuseppemarletta.auth_service.dto.LoginRequest;
import com.github.giuseppemarletta.auth_service.dto.RegisterRequest;
import com.github.giuseppemarletta.auth_service.service.JwtService;

import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Optional;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final List<String> VALID_ROLES = Arrays.asList("USER", "MODERATOR", "ADMIN");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Optional<User> userOptional = userRepository.findByEmail(loginRequest.getEmail());
        if(userOptional.isPresent() && passwordEncoder.matches(loginRequest.getPassword(), userOptional.get().getPassword())) {
            String token = jwtService.generateToken(userOptional.get());
            return ResponseEntity.ok(new JwtResponse(token)); 
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        if(userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        String role = registerRequest.getRole() != null ? registerRequest.getRole().toUpperCase() : "USER";
        if (!VALID_ROLES.contains(role)) {
            return ResponseEntity.badRequest().body("Invalid role. Valid roles are: " + VALID_ROLES);
        }

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(role);
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
