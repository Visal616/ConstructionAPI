package com.example.contructioninventoryapi.service;

import com.example.contructioninventoryapi.entity.Company;
import com.example.contructioninventoryapi.entity.User;
import com.example.contructioninventoryapi.repository.CompanyRepository;
import com.example.contructioninventoryapi.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public CompanyService(CompanyRepository companyRepository, UserRepository userRepository) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    // --- ADD THIS MISSING METHOD ---
    public List<Company> getAll() {
        return companyRepository.findAll();
    }
    // -------------------------------

    @Transactional
    public Company createCompany(Company company, String adminEmail) {
        company.setCompanyId(UUID.randomUUID().toString());
        company.setRegisterDate(LocalDateTime.now());

        Company savedCompany = companyRepository.save(company);

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

        admin.setCompany(savedCompany);
        userRepository.save(admin);

        return savedCompany;
    }
}