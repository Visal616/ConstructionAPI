package com.example.contructioninventoryapi.repository;

import com.example.contructioninventoryapi.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {

    @Query("SELECT c FROM Category c WHERE c.companyId = :companyId AND " +
            "(LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Category> searchCategories(String companyId, String search, Pageable pageable);

    Page<Category> findByCompanyId(String companyId, Pageable pageable);

    List<Category> findByCompanyId(String companyId);
}