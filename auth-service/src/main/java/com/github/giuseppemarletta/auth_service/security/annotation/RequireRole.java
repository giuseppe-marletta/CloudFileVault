package com.github.giuseppemarletta.auth_service.security.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})   
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {
    String[] value(); //lista dei ruoli ammessi 
}
