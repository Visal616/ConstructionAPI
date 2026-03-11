package com.example.contructioninventoryapi.repository;

import com.example.contructioninventoryapi.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, String> {

    @Query("SELECT od FROM OrderDetail od WHERE od.order.orderDate BETWEEN :startDate AND :endDate")
    List<OrderDetail> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
