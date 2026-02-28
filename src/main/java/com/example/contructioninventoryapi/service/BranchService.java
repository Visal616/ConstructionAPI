package com.example.contructioninventoryapi.service;

import com.example.contructioninventoryapi.entity.Branch;
import com.example.contructioninventoryapi.entity.Company;
import com.example.contructioninventoryapi.repository.BranchRepository;
import com.example.contructioninventoryapi.repository.CompanyRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BranchService {

    private final BranchRepository branchRepository;
    private final CompanyRepository companyRepository;

    public BranchService(BranchRepository branchRepository, CompanyRepository companyRepository) {
        this.branchRepository = branchRepository;
        this.companyRepository = companyRepository;
    }

    public List<Branch> getAllBranches() {
        return branchRepository.findAll();
    }

    public List<Branch> getBranchesByCompany(String companyId) {
        return branchRepository.findByCompany_CompanyId(companyId);
    }

    public Branch createBranch(Branch branch, String companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));

        branch.setBranchId(UUID.randomUUID().toString());
        branch.setCreatedAt(LocalDateTime.now());
        branch.setCompany(company); // Link branch to company

        return branchRepository.save(branch);
    }

    public Branch updateBranch(String id, Branch branchDetails, String companyId) {
        return branchRepository.findById(id).map(branch -> {
            branch.setBranchName(branchDetails.getBranchName());
            branch.setLocation(branchDetails.getLocation());
            branch.setContactNumber(branchDetails.getContactNumber());

            // Allow changing company if needed
            if (companyId != null && !companyId.equals(branch.getCompany().getCompanyId())) {
                Company newCompany = companyRepository.findById(companyId)
                        .orElseThrow(() -> new RuntimeException("Company not found"));
                branch.setCompany(newCompany);
            }

            return branchRepository.save(branch);
        }).orElseThrow(() -> new RuntimeException("Branch not found"));
    }

    public void deleteBranch(String id) {
        branchRepository.deleteById(id);
    }
}