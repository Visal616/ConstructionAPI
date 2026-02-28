package com.example.contructioninventoryapi.controller;

import com.example.contructioninventoryapi.entity.Role;
import com.example.contructioninventoryapi.entity.User;
import com.example.contructioninventoryapi.repository.RoleRepository;
import com.example.contructioninventoryapi.repository.UserRepository;
import com.example.contructioninventoryapi.security.JwtUtils;
import com.example.contructioninventoryapi.service.TwoFactorService;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final TwoFactorService twoFactorService;

    public AuthController(UserRepository userRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtils jwtUtils,
                          TwoFactorService twoFactorService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.twoFactorService = twoFactorService;
    }

    // --- LOGIN ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isPresent() && passwordEncoder.matches(request.getPassword(), userOpt.get().getPasswordHash())) {
            User user = userOpt.get();

            if (Boolean.FALSE.equals(user.getStatus())) {
                return ResponseEntity.status(403).body(new AuthResponse("ERROR", "Inactive Account"));
            }

            // Check the boolean flag (Source of Truth)
            if (user.isTwoFactorEnabled()) {
                return ResponseEntity.ok(new AuthResponse("2FA_REQUIRED", null));
            }

            // Normal Login (No 2FA)
            String roleName = (user.getRole() != null) ? user.getRole().getRoleName() : "STAFF";
            String token = jwtUtils.generateToken(user.getEmail(), roleName);
            return ResponseEntity.ok(new AuthResponse("SUCCESS", token));
        }
        return ResponseEntity.status(401).body("Invalid credentials");
    }

    // 1. SETUP: Generate secret & QR
    @PostMapping("/setup-2fa")
    public ResponseEntity<?> setup2fa(@RequestParam String email) {
        // Ensure user exists, but we don't need to store the user object in a variable
        if (userRepository.findByEmail(email).isEmpty()) {
            throw new RuntimeException("User not found");
        }

        String secret = twoFactorService.generateNewSecret();
        String qrCodeUri = twoFactorService.generateQrCodeImageUri(secret, email);

        return ResponseEntity.ok(new TwoFactorSetupResponse(secret, qrCodeUri));
    }

    // 2. ACTIVATE: Verify code -> Enable Flag -> Save Secret
    @PostMapping("/activate-2fa")
    public ResponseEntity<?> activate2fa(@RequestBody Activate2FARequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (twoFactorService.isOtpValid(request.getSecret(), request.getCode())) {
            // Sync: Save Secret AND Enable Boolean
            user.setTwoFactorSecret(request.getSecret());
            user.setTwoFactorEnabled(true);
            userRepository.save(user);

            return ResponseEntity.ok("2FA Activated Successfully");
        }
        return ResponseEntity.status(401).body("Invalid Code");
    }

    // 3. DISABLE: Verify code -> Disable Flag -> Clear Secret
    @PostMapping("/disable-2fa")
    public ResponseEntity<?> disable2fa(@RequestBody TwoFactorRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Use current stored secret to verify identity
        if (user.getTwoFactorSecret() != null && twoFactorService.isOtpValid(user.getTwoFactorSecret(), request.getCode())) {
            // Sync: Clear Secret AND Disable Boolean
            user.setTwoFactorSecret(null);
            user.setTwoFactorEnabled(false);
            userRepository.save(user);

            return ResponseEntity.ok("2FA Disabled Successfully");
        }
        return ResponseEntity.status(401).body("Invalid Code");
    }

    // 4. VERIFY: Used during Login to get the JWT Token
    @PostMapping("/verify-2fa")
    public ResponseEntity<?> verify2fa(@RequestBody TwoFactorRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getTwoFactorSecret() != null && twoFactorService.isOtpValid(user.getTwoFactorSecret(), request.getCode())) {
            String roleName = (user.getRole() != null) ? user.getRole().getRoleName() : "STAFF";
            String token = jwtUtils.generateToken(user.getEmail(), roleName);
            return ResponseEntity.ok(new AuthResponse("SUCCESS", token));
        }
        return ResponseEntity.status(401).body("Invalid 2FA Code");
    }

    // --- PASSWORD RESET VIA 2FA ---
    @PostMapping("/reset-password-2fa")
    public ResponseEntity<?> resetPasswordWith2fa(@RequestBody ResetPassword2FARequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isTwoFactorEnabled() || user.getTwoFactorSecret() == null) {
            return ResponseEntity.badRequest().body("2FA is not enabled for this account.");
        }

        if (!twoFactorService.isOtpValid(user.getTwoFactorSecret(), request.getCode())) {
            return ResponseEntity.status(401).body(new AuthResponse("ERROR", "Invalid 2FA Code"));
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.ok(new AuthResponse("SUCCESS", "Password updated successfully."));
    }

    // --- REGISTRATION ---
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists");
        }
        User newUser = new User();
        newUser.setUserId(UUID.randomUUID().toString());
        newUser.setFullName(request.getFullName());
        newUser.setEmail(request.getEmail());
        newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        newUser.setStatus(true);
        newUser.setTwoFactorEnabled(false); // Default to false

        Role adminRole = roleRepository.findByRoleName("ADMIN")
                .orElseThrow(() -> new RuntimeException("Error: Role 'ADMIN' is not found."));
        newUser.setRole(adminRole);

        userRepository.save(newUser);
        return ResponseEntity.ok("User registered successfully!");
    }

    // ==========================================
    // INNER STATIC CLASSES (DTOs)
    // Moving them inside makes them accessible without visibility errors
    // ==========================================

    @Data
    public static class RegisterRequest {
        private String fullName;
        private String email;
        private String password;
    }

    @Data
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @Data
    public static class TwoFactorRequest {
        private String email;
        private String code;
    }

    @Data
    public static class Activate2FARequest {
        private String email;
        private String secret;
        private String code;
    }

    @Data
    public static class AuthResponse {
        private String status;
        private String token;
        public AuthResponse(String s, String t) { this.status = s; this.token = t; }
    }

    @Data
    public static class TwoFactorSetupResponse {
        private String secret;
        private String qrCodeUri;
        public TwoFactorSetupResponse(String s, String q) { this.secret = s; this.qrCodeUri = q; }
    }

    @Data
    public static class ResetPassword2FARequest {
        private String email;
        private String code;
        private String newPassword;
    }
}