package com.example.contructioninventoryapi.repository;

import com.example.contructioninventoryapi.entity.Branch;
import com.example.contructioninventoryapi.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BranchRepository extends JpaRepository<Branch, String> {
    List<Branch> findByCompany(Company company);
    List<Branch> findByCompanyCompanyId(String companyId);
}