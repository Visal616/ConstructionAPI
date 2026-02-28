package com.example.contructioninventoryapi.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductRequest {
    private String productName;
    private String categoryId;
    private String companyId;    // From context
    private String baseUnit;     // e.g., "Pcs", "Kg"
    private String purchaseUnit; // e.g., "Box", "Sack"
    private Integer conversionRate; // e.g., 1 Box = 50 Pcs (Default: 1)
    private BigDecimal costPrice;
    private BigDecimal unitPrice;
    private String productImageUrl;
}