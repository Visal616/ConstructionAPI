package com.example.contructioninventoryapi.repository;

import com.example.contructioninventoryapi.dto.SalesSummaryDTO;
import com.example.contructioninventoryapi.entity.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    @Query("SELECT o FROM Order o WHERE o.paymentStatus IN ('Unpaid', 'Partial') " +
            "AND (cast(:startDate as timestamp) IS NULL OR o.orderDate >= :startDate) " +
            "AND (cast(:endDate as timestamp) IS NULL OR o.orderDate <= :endDate)")
    List<Order> findUnpaidOrders(@Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate);

    //Report Query
    @Query("SELECT new com.example.contructioninventoryapi.dto.SalesSummaryDTO(" +
            "o.orderId, o.orderDate, b.branchName, c.customerName, " +
            "o.totalAmount, o.discountAmount, " +
            "(SELECT COALESCE(SUM(p.amountPaid), 0) FROM Payment p WHERE p.invoice = o.invoice), " +
            "o.createdBy) " +
            "FROM Order o " +
            "LEFT JOIN o.branch b " +
            "LEFT JOIN o.customer c " +
            "WHERE (:branchId IS NULL OR :branchId = '' OR b.branchId = :branchId) " +
            "AND (:startDate IS NULL OR o.orderDate >= :startDate) " +
            "AND (:endDate IS NULL OR o.orderDate <= :endDate) " +
            "ORDER BY o.orderDate DESC")
    List<SalesSummaryDTO> getSalesSummaryReport(
            @Param("branchId") String branchId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    List<Order> findByBranchBranchIdOrderByOrderDateDesc(String branchId);
}
