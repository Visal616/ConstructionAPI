package com.example.contructioninventoryapi.repository;

import com.example.contructioninventoryapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    // This is the method required by CustomUserDetailsService
    Optional<User> findByEmail(String email);
}