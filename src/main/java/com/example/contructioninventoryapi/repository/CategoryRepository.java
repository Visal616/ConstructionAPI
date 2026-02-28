package com.example.contructioninventoryapi.repository;

import com.example.contructioninventoryapi.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryRepository extends JpaRepository<Category, String> {
    Page<Category> findByCategoryNameContainingIgnoreCase(String name, Pageable pageable);
}