package com.example.contructioninventoryapi.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class SalesRequest {
    private String customerId; // Can be null for "Walk-in Customer"
    private String branchId;
    private String status;
    private String paymentStatus;
    private BigDecimal discountAmount;
    private String createdBy;

    private BigDecimal amountPaid;
    private String paymentMethod;
    private String saleType;

    private List<SalesItemDto> items;

    @Data
    public static class SalesItemDto {
        private String productId;
        private int quantity;
        private BigDecimal unitPrice; // Price sold at
        private BigDecimal discount;  // Item level discount (optional)
    }
}