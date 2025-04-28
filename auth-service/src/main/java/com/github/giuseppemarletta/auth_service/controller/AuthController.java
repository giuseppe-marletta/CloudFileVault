package com.github.giuseppemarletta.auth_service.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.giuseppemarletta.auth_service.Repository.UserRepository;
import com.github.giuseppemarletta.auth_service.model.User;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Jwts;

import com.github.giuseppemarletta.auth_service.dto.JwtResponse;
import com.github.giuseppemarletta.auth_service.dto.LoginRequest;
import com.github.giuseppemarletta.auth_service.dto.RegisterRequest;

import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Optional;
import java.util.Date;
import java.security.Key;
import io.jsonwebtoken.security.Keys;



@RestController
@RequestMapping("/auth")
public class AuthController {

    @Value("${jwt.secret}")
        private String jwtSecret;

    
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Optional<User> userOptional = userRepository.findByEmail(loginRequest.getEmail());
        if(userOptional.isPresent() && passwordEncoder.matches(loginRequest.getPassword(), userOptional.get().getPassword())) {
            String token = generateJwtToken(userOptional.get());
            return ResponseEntity.ok(new JwtResponse(token)); // 200 OK
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401 Unauthorized
    }


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        if(userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // 409 Conflict - Email already exists
        }

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole("USER"); // Default role
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).build(); // 201 Created

    }


    private String generateJwtToken(User user) {

        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.builder() // Create JWT token 
                .setSubject(user.getEmail()) // Set subject to email
                .setIssuedAt(new Date()) // Set issued date to now
                .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour expiration
                .signWith(key, SignatureAlgorithm.HS256) // Sign with HS512 algorithm
                .compact(); // Compact the JWT token
    }
    
}
