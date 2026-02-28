package com.example.contructioninventoryapi.repository;

import com.example.contructioninventoryapi.entity.BranchProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchProductRepository extends JpaRepository<BranchProduct, String> {

    List<BranchProduct> findByBranch_BranchId(String branchId);

    Optional<BranchProduct> findByBranch_BranchIdAndProduct_ProductId(String branchId, String productId);

    List<BranchProduct> findByBranch_BranchIdAndQuantityLessThanEqual(String branchId, Integer reorderLevel);

    void deleteByProduct_ProductId(String productId);
}