package com.github.giuseppemarletta.auth_service.security.aspect;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.github.giuseppemarletta.auth_service.security.annotation.RequireRole;
import com.github.giuseppemarletta.auth_service.service.JwtService;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
public class RoleAspect {

   // private final JwtService jwtService;
    private final HttpServletRequest request;
    private final JwtService jwtService;

    @Around("@annotation(com.github.giuseppemarletta.auth_service.security.annotation.RequireRole) || @within(com.github.giuseppemarletta.auth_service.security.annotation.RequireRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint) throws Throwable {
        
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        RequireRole requireRole = method.getAnnotation(RequireRole.class);

        if (requireRole == null) {
            requireRole = joinPoint.getTarget().getClass().getAnnotation(RequireRole.class);
        }

        if(requireRole == null) {
            return joinPoint.proceed();
        }

        String authHeader = request.getHeader("Authorization");
        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        String token = authHeader.substring(7);
        Claims claims = jwtService.extractAllClaims(token);

        String userRole = "ROLE_" + claims.get("role", String.class);

        if(Arrays.stream(requireRole.value())
                .map(role -> "ROLE_" + role)
                .noneMatch(r -> r.equals(userRole))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to access this resource");
        }

        return joinPoint.proceed();
    }
}
        
