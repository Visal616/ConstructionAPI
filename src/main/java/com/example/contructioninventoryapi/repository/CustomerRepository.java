package com.example.contructioninventoryapi.repository;

import com.example.contructioninventoryapi.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {

    @Query("SELECT c FROM Customer c WHERE c.companyId = :companyId AND " +
            "(LOWER(c.customerName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.contactName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "c.phone LIKE CONCAT('%', :search, '%'))")
    Page<Customer> searchCustomers(String companyId, String search, Pageable pageable);

    Page<Customer> findByCompanyId(String companyId, Pageable pageable);
}