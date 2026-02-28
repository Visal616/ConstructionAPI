package com.example.contructioninventoryapi.repository;

import com.example.contructioninventoryapi.entity.PurchasePayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchasePaymentRepository extends JpaRepository<PurchasePayment, Long> {
}