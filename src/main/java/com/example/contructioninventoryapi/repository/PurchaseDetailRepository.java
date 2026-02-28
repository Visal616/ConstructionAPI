package com.example.contructioninventoryapi.repository;

import com.example.contructioninventoryapi.entity.Purchase;
import com.example.contructioninventoryapi.entity.PurchaseDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseDetailRepository extends JpaRepository<PurchaseDetail, String> {
    List<PurchaseDetail> findByPurchase(Purchase purchase);
}
