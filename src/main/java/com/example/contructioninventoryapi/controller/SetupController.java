package com.example.contructioninventoryapi.controller;

import com.example.contructioninventoryapi.entity.Branch;
import com.example.contructioninventoryapi.entity.Company;
import com.example.contructioninventoryapi.entity.User;
import com.example.contructioninventoryapi.repository.BranchRepository;
import com.example.contructioninventoryapi.repository.CompanyRepository;
import com.example.contructioninventoryapi.repository.UserRepository;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/setup")
public class SetupController {

    private final CompanyRepository companyRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;

    public SetupController(CompanyRepository companyRepository,
                           BranchRepository branchRepository,
                           UserRepository userRepository) {
        this.companyRepository = companyRepository;
        this.branchRepository = branchRepository;
        this.userRepository = userRepository;
    }

    // 1. Create Company
    @PostMapping("/company")
    @Transactional
    public ResponseEntity<?> createCompany(@RequestBody CompanyRequest request, Principal principal) {
        try {
            if (principal == null) return ResponseEntity.status(401).body("Unauthorized");

            String email = principal.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Company company = new Company();
            // REMOVED: company.setCompanyId(...) - Let Database do it!
            company.setCompanyName(request.getCompanyName());
            company.setAddress(request.getAddress());
            company.setEmail(request.getEmail());
            company.setContactNumber(request.getContactNumber());
            company.setRegisterDate(LocalDateTime.now());

            Company savedCompany = companyRepository.save(company);

            user.setCompany(savedCompany);
            userRepository.save(user);

            return ResponseEntity.ok(savedCompany);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // 2. Create Branch
    @PostMapping("/branch")
    @Transactional // Ensure the link to user is saved safely
    public ResponseEntity<?> createBranch(@RequestBody BranchRequest request, Principal principal) {
        try {
            if (principal == null) return ResponseEntity.status(401).body("Unauthorized");

            String email = principal.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (user.getCompany() == null) {
                return ResponseEntity.badRequest().body("You must create a company first.");
            }

            // 1. Create and Save the Branch
            Branch branch = new Branch();
            branch.setBranchName(request.getBranchName());
            branch.setLocation(request.getLocation());
            branch.setContactNumber(request.getContactNumber());
            branch.setCreatedAt(LocalDateTime.now());
            branch.setCompany(user.getCompany());

            Branch savedBranch = branchRepository.save(branch);

            // 2. IMPORTANT: Link this branch to the User
            // This populates the user_branches table so it shows on the Dashboard
            user.getBranches().add(savedBranch);
            userRepository.save(user);

            return ResponseEntity.ok(savedBranch);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error creating branch: " + e.getMessage());
        }
    }
}

// --- DTO Classes ---
@Data class CompanyRequest { private String companyName; private String address; private String email; private String contactNumber; }
@Data class BranchRequest { private String branchName; private String location; private String contactNumber; }