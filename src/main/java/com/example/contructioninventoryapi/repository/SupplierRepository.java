package com.example.contructioninventoryapi.repository;

import com.example.contructioninventoryapi.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, String> {

    // Search by Supplier Name, Contact Name, or Phone
    @Query("SELECT s FROM Supplier s WHERE s.companyId = :companyId AND " +
            "(LOWER(s.supplierName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.contactName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "s.phone LIKE CONCAT('%', :search, '%'))")
    Page<Supplier> searchSuppliers(String companyId, String search, Pageable pageable);

    // Get all for company (with pagination)
    Page<Supplier> findByCompanyId(String companyId, Pageable pageable);
}