package com.example.contructioninventoryapi.controller;

import com.example.contructioninventoryapi.dto.UserResponse;
import com.example.contructioninventoryapi.entity.User;
import com.example.contructioninventoryapi.service.FileStorageService;
import com.example.contructioninventoryapi.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final FileStorageService fileStorageService;

    public UserController(UserService userService, FileStorageService fileStorageService) {
        this.userService = userService;
        this.fileStorageService = fileStorageService;
    }

    // --- DASHBOARD ENDPOINT ---
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body("Unauthorized");
        UserResponse response = userService.getUserProfile(principal.getName());
        return ResponseEntity.ok(response);
    }

    // --- CRUD METHODS ---
    @GetMapping
    public List<User> getAll() {
        return userService.getAllUsers();
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<User>> getUsersByCompany(@PathVariable String companyId) {
        return ResponseEntity.ok(userService.getUsersByCompanyId(companyId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable String id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Return a simple message instead of the raw User entity to avoid Infinite Recursion
    @PostMapping
    public ResponseEntity<?> create(@RequestBody User user) {
        userService.createUser(user);
        return ResponseEntity.ok(Map.of("message", "User created successfully!"));
    }

    // Return a simple message instead of the raw User entity to avoid Infinite Recursion
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody User user) {
        userService.updateUser(id, user);
        return ResponseEntity.ok(Map.of("message", "User updated successfully!"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    // --- FILE UPLOAD METHOD ---
    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }

        try {
            String fileName = fileStorageService.storeFile(file);
            return ResponseEntity.ok(Map.of("imageUrl", fileName));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to upload image: " + e.getMessage()));
        }
    }
}