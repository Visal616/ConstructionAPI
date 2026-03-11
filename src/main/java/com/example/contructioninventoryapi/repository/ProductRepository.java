package com.example.contructioninventoryapi.repository;

import com.example.contructioninventoryapi.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    // Fetch all products defined by this company (Master List)
    List<Product> findByCompanyId(String companyId);
}