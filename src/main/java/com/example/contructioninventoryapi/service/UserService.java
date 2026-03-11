package com.example.contructioninventoryapi.service;

import com.example.contructioninventoryapi.dto.UserResponse;
import com.example.contructioninventoryapi.entity.Branch;
import com.example.contructioninventoryapi.entity.Company;
import com.example.contructioninventoryapi.entity.Role;
import com.example.contructioninventoryapi.entity.User;
import com.example.contructioninventoryapi.repository.BranchRepository;
import com.example.contructioninventoryapi.repository.CompanyRepository;
import com.example.contructioninventoryapi.repository.RoleRepository;
import com.example.contructioninventoryapi.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CompanyRepository companyRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       CompanyRepository companyRepository,
                       BranchRepository branchRepository,
                       PasswordEncoder passwordEncoder,
                       FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.companyRepository = companyRepository;
        this.branchRepository = branchRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileStorageService = fileStorageService;
    }

    // --- GET METHODS ---

    public List<User> getUsersByCompanyId(String companyId) {
        // NOTE: Ensure UserRepository has findByCompanyCompanyId(String companyId)
        return userRepository.findByCompanyCompanyId(companyId);
    }

    public UserResponse getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToUserResponse(user);
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setUserId(user.getUserId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setTwoFactorEnabled(user.isTwoFactorEnabled());
        response.setProfileImageUrl(user.getProfileImageUrl());

        if (user.getRole() != null) {
            response.setRole(user.getRole().getRoleName());
        }

        if (user.getCompany() != null) {
            UserResponse.CompanyInfo cInfo = new UserResponse.CompanyInfo();
            cInfo.setCompanyId(user.getCompany().getCompanyId());
            cInfo.setCompanyName(user.getCompany().getCompanyName());
            cInfo.setAddress(user.getCompany().getAddress());
            cInfo.setContactNumber(user.getCompany().getContactNumber());
            cInfo.setEmail(user.getCompany().getEmail());
            response.setCompany(cInfo);
        }

        if (user.getBranches() != null && !user.getBranches().isEmpty()) {
            List<UserResponse.BranchInfo> branchDTOs = user.getBranches().stream()
                    .map(branch -> {
                        UserResponse.BranchInfo bInfo = new UserResponse.BranchInfo();
                        bInfo.setBranchId(branch.getBranchId());
                        bInfo.setBranchName(branch.getBranchName());
                        bInfo.setLocation(branch.getLocation());
                        bInfo.setContactNumber(branch.getContactNumber());
                        return bInfo;
                    }).collect(Collectors.toList());
            response.setBranches(branchDTOs);
        } else {
            response.setBranches(new ArrayList<>());
        }

        return response;
    }

    // --- CREATE USER ---
    @Transactional
    public User createUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        user.setUserId(UUID.randomUUID().toString());

        // SAFELY encode password
        if (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }

        // Assign Role
        if (user.getRole() != null && user.getRole().getRoleId() != null) {
            Role role = roleRepository.findById(user.getRole().getRoleId()).orElse(null);
            user.setRole(role);
        }

        // Assign Company
        if (user.getCompany() != null && user.getCompany().getCompanyId() != null) {
            Company company = companyRepository.findById(user.getCompany().getCompanyId()).orElse(null);
            user.setCompany(company);
        }

        // Assign Branches (Optimized)
        if (user.getBranches() != null && !user.getBranches().isEmpty()) {
            List<String> branchIds = user.getBranches().stream()
                    .map(Branch::getBranchId)
                    .collect(Collectors.toList());
            user.setBranches(new HashSet<>(branchRepository.findAllById(branchIds)));
        }

        return userRepository.save(user);
    }

    // --- UPDATE USER ---
    @Transactional
    public User updateUser(String id, User userDetails) {
        return userRepository.findById(id).map(existingUser -> {

            // Image management
            if (userDetails.getProfileImageUrl() != null
                    && !userDetails.getProfileImageUrl().equals(existingUser.getProfileImageUrl())) {
                String oldFilename = extractFilenameFromUrl(existingUser.getProfileImageUrl());
                if (oldFilename != null) fileStorageService.deleteFile(oldFilename);
                existingUser.setProfileImageUrl(userDetails.getProfileImageUrl());
            }

            existingUser.setFullName(userDetails.getFullName());
            existingUser.setPhoneNumber(userDetails.getPhoneNumber());
            existingUser.setStatus(userDetails.getStatus());

            // Safely update password only if a new one is provided
            if (userDetails.getPasswordHash() != null && !userDetails.getPasswordHash().isEmpty()) {
                existingUser.setPasswordHash(passwordEncoder.encode(userDetails.getPasswordHash()));
            }

            // Update Role
            if (userDetails.getRole() != null && userDetails.getRole().getRoleId() != null) {
                Role role = roleRepository.findById(userDetails.getRole().getRoleId())
                        .orElseThrow(() -> new RuntimeException("Role not found"));
                existingUser.setRole(role);
            }

            // Update Company
            if (userDetails.getCompany() != null && userDetails.getCompany().getCompanyId() != null) {
                Company company = companyRepository.findById(userDetails.getCompany().getCompanyId())
                        .orElse(null);
                existingUser.setCompany(company);
            } else if (userDetails.getCompany() == null) {
                existingUser.setCompany(null);
            }

            // Update Branches (Optimized)
            if (userDetails.getBranches() != null) {
                List<String> branchIds = userDetails.getBranches().stream()
                        .map(Branch::getBranchId)
                        .collect(Collectors.toList());
                existingUser.setBranches(new HashSet<>(branchRepository.findAllById(branchIds)));
            }

            return userRepository.save(existingUser);
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }

    // --- DELETE USER ---
    @Transactional
    public void deleteUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            String filename = extractFilenameFromUrl(user.getProfileImageUrl());
            if (filename != null) fileStorageService.deleteFile(filename);
        }
        userRepository.delete(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    private String extractFilenameFromUrl(String url) {
        if (url == null || url.isEmpty()) return null;
        try {
            return url.substring(url.lastIndexOf("/") + 1);
        } catch (Exception e) { return null; }
    }
}