package com.example.contructioninventoryapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesSummaryDTO {
    private String orderId;
    private LocalDateTime orderDate;
    private String branchName;
    private String customerName;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal amountPaid;
    private String salesperson;
}