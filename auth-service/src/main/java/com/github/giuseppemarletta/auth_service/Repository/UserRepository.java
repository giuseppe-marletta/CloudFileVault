package com.github.giuseppemarletta.auth_service.Repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;

import com.github.giuseppemarletta.auth_service.model.User; //user class

@Repository
@EnableScan
public interface UserRepository extends CrudRepository<User, String> { // Fix the missing closing parenthesis
    
    Optional<User> findByEmail(String email);
    
}
