package com.example.contructioninventoryapi.repository;

import com.example.contructioninventoryapi.entity.TransferDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransferDetailRepository extends JpaRepository<TransferDetail, UUID> {

    @Query("SELECT td FROM TransferDetail td WHERE td.transfer.transferDate BETWEEN :startDate AND :endDate")
    List<TransferDetail> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}