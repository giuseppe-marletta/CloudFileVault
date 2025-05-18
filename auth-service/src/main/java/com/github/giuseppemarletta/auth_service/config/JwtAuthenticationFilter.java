package com.github.giuseppemarletta.auth_service.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;
import java.io.IOException;
import org.springframework.lang.NonNull;

import com.github.giuseppemarletta.auth_service.Repository.UserRepository;
import com.github.giuseppemarletta.auth_service.model.User; 
import com.github.giuseppemarletta.auth_service.service.JwtService;

/**
 * JwtAuthenticationFilter is a filter that checks for JWT tokens in the request headers.
 * If a valid token is found, it authenticates the user and sets the security context.
 */

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                  @NonNull HttpServletResponse response, 
                                  @NonNull FilterChain filterChain) throws ServletException, IOException {

        System.out.println("[JwtAuthenticationFilter] Incoming request to: " + request.getRequestURI());
        String token = jwtService.extractToken(request.getHeader("Authorization"));

        if (token != null && !token.isEmpty()) {
            String username = jwtService.validateTokenAndGetUsername(token);
            if (username != null) {
                User user = userRepository.findByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
                
                // Create authority from user role
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole());
                
                // Create authentication with user authorities
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user, 
                    null, 
                    Collections.singletonList(authority)
                );
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }
}
