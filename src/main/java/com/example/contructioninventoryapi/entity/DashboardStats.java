package com.example.contructioninventoryapi.entity;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DashboardStats {
    private long totalProducts;
    private long totalCategories;
    private long lowStockCount;
    private BigDecimal totalInventoryValue;
}