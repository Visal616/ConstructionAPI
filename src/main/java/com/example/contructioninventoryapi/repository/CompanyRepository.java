package com.example.contructioninventoryapi.repository;

import com.example.contructioninventoryapi.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, String> {
    boolean existsByCompanyName(String companyName);
}