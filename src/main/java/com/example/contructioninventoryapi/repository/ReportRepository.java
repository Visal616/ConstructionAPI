package com.example.contructioninventoryapi.repository;

import com.example.contructioninventoryapi.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, String> {

    List<Report> findByBranch_BranchId(String branchId);

}