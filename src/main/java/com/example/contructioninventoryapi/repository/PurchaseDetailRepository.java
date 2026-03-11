package com.example.contructioninventoryapi.repository;

import com.example.contructioninventoryapi.entity.Purchase;
import com.example.contructioninventoryapi.entity.PurchaseDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PurchaseDetailRepository extends JpaRepository<PurchaseDetail, String> {
    List<PurchaseDetail> findByPurchase(Purchase purchase);

    @Query("SELECT pd FROM PurchaseDetail pd WHERE pd.purchase.purchaseDate BETWEEN :startDate AND :endDate")
    List<PurchaseDetail> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
