package com.example.contructioninventoryapi.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PurchaseRequest {
    private String supplierId;
    private String branchId;
    private String description;
    private String status;      // "Pending" or "Received"
    private BigDecimal totalCost;
    private List<PurchaseItemDto> items; // List of products

    private String paymentStatus;
    private BigDecimal amountPaid;
    private String paymentMethod;

    @Data
    public static class PurchaseItemDto {
        private String productId;
        private int quantity;
        private BigDecimal unitCost;
    }
}