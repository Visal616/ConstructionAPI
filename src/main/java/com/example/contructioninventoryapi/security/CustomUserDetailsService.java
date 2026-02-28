package com.example.contructioninventoryapi.security;

import com.example.contructioninventoryapi.entity.User;
import com.example.contructioninventoryapi.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Fetch User
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // 2. Check Status (Optional logic: Block login if status is false)
        if (Boolean.FALSE.equals(user.getStatus())) {
            throw new UsernameNotFoundException("User is inactive");
        }

        // 3. Handle Role (New Logic: Default to 'USER' if role is missing)
        String roleName = (user.getRole() != null) ? user.getRole().getRoleName() : "USER";

        // 4. Return Security User
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash()) // This must match the BCrypt hash in DB
                .authorities(new SimpleGrantedAuthority(roleName))
                .build();
    }
}