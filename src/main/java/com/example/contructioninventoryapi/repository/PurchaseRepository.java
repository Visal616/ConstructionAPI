package com.example.contructioninventoryapi.repository;

import com.example.contructioninventoryapi.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, String> {

    List<Purchase> findAllByPurchaseDateBetween(LocalDateTime start, LocalDateTime end);

    List<Purchase> findByBranchIdOrderByPurchaseDateDesc(String branchId);
}