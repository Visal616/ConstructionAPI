package com.example.contructioninventoryapi.controller;

import com.example.contructioninventoryapi.entity.Branch;
import com.example.contructioninventoryapi.entity.User;
import com.example.contructioninventoryapi.repository.UserRepository;
import com.example.contructioninventoryapi.service.BranchService;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping("/api/branches")
public class BranchController {

    private final BranchService branchService;
    private final UserRepository userRepository;

    // Inject both the BranchService and the UserRepository
    public BranchController(BranchService branchService, UserRepository userRepository) {
        this.branchService = branchService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<Branch> getAll() {
        return branchService.getAllBranches();
    }

    @GetMapping("/company/{companyId}")
    public List<Branch> getByCompany(@PathVariable String companyId) {
        return branchService.getBranchesByCompany(companyId);
    }

    @PostMapping
    @Transactional // Critical: Ensures branch saving and user linking happen together securely
    public ResponseEntity<Branch> create(@RequestBody BranchRequest request, Principal principal) {

        Branch branch = new Branch();
        branch.setBranchName(request.getBranchName());
        branch.setLocation(request.getLocation());
        branch.setContactNumber(request.getContactNumber());

        // 1. Create the branch and link it to the Company (via your BranchService)
        Branch savedBranch = branchService.createBranch(branch, request.getCompanyId());

        // 2. Link the new branch to the Admin who created it
        if (principal != null) {
            User user = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Safety check: if user has no branches yet, initialize an empty set to prevent a crash
            if (user.getBranches() == null) {
                user.setBranches(new HashSet<>());
            }

            // Add the new branch to the user's allowed branches
            user.getBranches().add(savedBranch);

            // Save the user to update the 'user_branches' join table
            userRepository.save(user);
        }

        return ResponseEntity.ok(savedBranch);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Branch> update(@PathVariable String id, @RequestBody BranchRequest request) {
        Branch branchDetails = new Branch();
        branchDetails.setBranchName(request.getBranchName());
        branchDetails.setLocation(request.getLocation());
        branchDetails.setContactNumber(request.getContactNumber());

        return ResponseEntity.ok(branchService.updateBranch(id, branchDetails, request.getCompanyId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        branchService.deleteBranch(id);
        return ResponseEntity.ok("Branch deleted successfully");
    }

    // Simple DTO for requests to match the React payload
    @Data
    public static class BranchRequest {
        private String branchName;
        private String location;
        private String contactNumber;
        private String companyId;
    }
}