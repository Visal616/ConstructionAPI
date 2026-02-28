package com.example.contructioninventoryapi.controller;

import com.example.contructioninventoryapi.entity.Branch;
import com.example.contructioninventoryapi.service.BranchService;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branches")
public class BranchController {

    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
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
    public ResponseEntity<Branch> create(@RequestBody BranchRequest request) {
        Branch branch = new Branch();
        branch.setBranchName(request.getBranchName());
        branch.setLocation(request.getLocation());
        branch.setContactNumber(request.getContactNumber());

        return ResponseEntity.ok(branchService.createBranch(branch, request.getCompanyId()));
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

    // Simple DTO for requests
    @Data
    public static class BranchRequest {
        private String branchName;
        private String location;
        private String contactNumber;
        private String companyId;
    }
}