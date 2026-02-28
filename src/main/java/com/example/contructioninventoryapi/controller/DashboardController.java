package com.example.contructioninventoryapi.controller;

import com.example.contructioninventoryapi.entity.BranchProduct;
import com.example.contructioninventoryapi.entity.DashboardStats;
import com.example.contructioninventoryapi.repository.BranchProductRepository; // 1. Import this
import com.example.contructioninventoryapi.repository.CategoryRepository;
import com.example.contructioninventoryapi.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:5173")
public class DashboardController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BranchProductRepository branchProductRepository; // 2. Inject this

    @GetMapping("/stats")
    public DashboardStats getStats() {
        DashboardStats stats = new DashboardStats();

        // 1. Basic Counts
        stats.setTotalProducts((int) productRepository.count());
        stats.setTotalCategories(categoryRepository.count());

        // 2. Fetch Inventory Data (Where the stock actually lives now)
        List<BranchProduct> inventoryList = branchProductRepository.findAll();

        // 3. Calculate Low Stock
        // We now check if the *Branch* specific stock is lower than the *Branch* specific reorder level
        long lowStock = inventoryList.stream()
                .filter(bp -> bp.getQuantity() <= bp.getReorderLevel())
                .count();
        stats.setLowStockCount(lowStock);

        // 4. Calculate Total Value
        // Logic: (Branch Quantity) * (Product Cost Price)
        BigDecimal totalValue = inventoryList.stream()
                .map(bp -> {
                    BigDecimal price = bp.getProduct().getCostPrice(); // Get price from parent Product
                    BigDecimal quantity = BigDecimal.valueOf(bp.getQuantity()); // Get Qty from Branch
                    return price.multiply(quantity);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        stats.setTotalInventoryValue(totalValue);

        return stats;
    }
}