package com.github.giuseppemarletta.auth_service.security.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.github.giuseppemarletta.auth_service.security.annotation.RequireRole;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;


@Aspect
@Component
@RequiredArgsConstructor
public class RoleAspect {

   // private final JwtService jwtService;
    private final HttpServletRequest request;

    
    public void checkRole(RequireRole requireRole) {
        String[] allowedRoles = requireRole.value();
        String currentRole = SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().getAuthority();
    }
}
