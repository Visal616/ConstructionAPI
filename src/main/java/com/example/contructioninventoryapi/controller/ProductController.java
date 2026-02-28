package com.example.contructioninventoryapi.controller;

import com.example.contructioninventoryapi.dto.ProductRequest;
import com.example.contructioninventoryapi.entity.BranchProduct;
import com.example.contructioninventoryapi.entity.Category;
import com.example.contructioninventoryapi.entity.Product;
import com.example.contructioninventoryapi.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.contructioninventoryapi.dto.ProductRequest;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // --- CATEGORIES ---
    @GetMapping("/categories")
    public List<Category> getCategories() {
        return productService.getAllCategories();
    }

    @PostMapping("/categories")
    public Category createCategory(@RequestBody Category category) {
        return productService.createCategory(category);
    }

    // --- MASTER PRODUCTS ---
    @GetMapping("/company/{companyId}")
    public List<Product> getByCompany(@PathVariable String companyId) {
        return productService.getCompanyProducts(companyId);
    }

    @PostMapping
    public Product createProduct(@RequestBody ProductRequest request) {
        return productService.createProduct(request);
    }

    // --- BRANCH INVENTORY (STOCK) ---
    @GetMapping("/branch/{branchId}")
    public List<BranchProduct> getBranchStock(@PathVariable String branchId) {
        return productService.getBranchInventory(branchId);
    }

    // Add Stock (Simple purchase)
    @PostMapping("/stock/add")
    public ResponseEntity<BranchProduct> addStock(
            @RequestParam String branchId,
            @RequestParam String productId,
            @RequestParam int quantity) {
        return ResponseEntity.ok(productService.addStock(branchId, productId, quantity));
    }

    // 1. UPDATE Master Product (Name, Price, Unit)
    @PutMapping("/{productId}")
    public ResponseEntity<Product> updateProduct(@PathVariable String productId, @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(productId, request));
    }

    // 2. DELETE Product (Master)
    @DeleteMapping("/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable String productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.ok("Product deleted successfully");
    }

    // 3. UPDATE Branch Settings (Reorder Level)
    @PutMapping("/branch-stock/{inventoryId}")
    public ResponseEntity<BranchProduct> updateStockSettings(
            @PathVariable String inventoryId,
            @RequestParam Integer reorderLevel) {
        return ResponseEntity.ok(productService.updateReorderLevel(inventoryId, reorderLevel));
    }
}