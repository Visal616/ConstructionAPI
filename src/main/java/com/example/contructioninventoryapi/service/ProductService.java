package com.example.contructioninventoryapi.service;

import com.example.contructioninventoryapi.dto.ProductRequest;
import com.example.contructioninventoryapi.entity.*;
import com.example.contructioninventoryapi.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BranchProductRepository branchProductRepository;
    private final CompanyRepository companyRepository;
    private final BranchRepository branchRepository;

    // Constructor Injection
    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          BranchProductRepository branchProductRepository,
                          CompanyRepository companyRepository,
                          BranchRepository branchRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.branchProductRepository = branchProductRepository;
        this.companyRepository = companyRepository;
        this.branchRepository = branchRepository;
    }

    public List<Category> getAllCategories(String companyId) {
        return categoryRepository.findByCompanyId(companyId);
    }

    public Category createCategory(Category category) {
        // FIXED: Removed manual UUID generation.
        // We let Hibernate's @GeneratedValue handle the ID automatically!
        return categoryRepository.save(category);
    }

    // --- 2. MASTER PRODUCT LOGIC ---
    public Product createProduct(ProductRequest request) {
        Product product = new Product();
        product.setProductId(UUID.randomUUID().toString()); // Or let Hibernate do it!
        product.setProductName(request.getProductName());
        product.setBaseUnit(request.getBaseUnit());
        product.setPurchaseUnit(request.getPurchaseUnit());
        product.setConversionRate(request.getConversionRate() != null ? request.getConversionRate() : 1);
        product.setCostPrice(request.getCostPrice());
        product.setUnitPrice(request.getUnitPrice());
        product.setProductImageUrl(request.getProductImageUrl());

        // We still look up Category because you likely need to display the Category Name in the frontend
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        product.setCategory(category);

        // FIXED: No more database lookup! Just attach the string ID directly.
        product.setCompanyId(request.getCompanyId());

        return productRepository.save(product);
    }

    // Get All Products for a Company (Master List)
    public List<Product> getCompanyProducts(String companyId) {
        return productRepository.findByCompanyId(companyId);
    }

    // --- 3. BRANCH STOCK LOGIC (The "Stock Engine") ---

    // Get Stock for a specific branch
    // If stock record doesn't exist, we return the product with 0 quantity (Virtual View)
    public List<BranchProduct> getBranchInventory(String branchId) {
        return branchProductRepository.findByBranch_BranchId(branchId);
    }

    @Transactional
    public BranchProduct addStock(String branchId, String productId, int quantityToAdd) {
        // 1. Check if stock record exists
        Optional<BranchProduct> existingStock = branchProductRepository
                .findByBranch_BranchIdAndProduct_ProductId(branchId, productId);

        if (existingStock.isPresent()) {
            // Update existing
            BranchProduct stock = existingStock.get();
            stock.setQuantity(stock.getQuantity() + quantityToAdd);
            return branchProductRepository.save(stock);
        } else {
            // Create new stock record
            BranchProduct newStock = new BranchProduct();
            newStock.setInventoryId(UUID.randomUUID().toString());
            newStock.setQuantity(quantityToAdd);
            newStock.setReorderLevel(10); // Default alert level

            Branch branch = branchRepository.findById(branchId)
                    .orElseThrow(() -> new RuntimeException("Branch not found"));
            newStock.setBranch(branch);

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            newStock.setProduct(product);

            return branchProductRepository.save(newStock);
        }
    }

    // Update Master Product
    public Product updateProduct(String productId, ProductRequest request) {
        return productRepository.findById(productId).map(product -> {
            product.setProductName(request.getProductName());
            product.setCostPrice(request.getCostPrice());
            product.setUnitPrice(request.getUnitPrice());
            product.setBaseUnit(request.getBaseUnit());
            product.setPurchaseUnit(request.getPurchaseUnit());
            product.setConversionRate(request.getConversionRate());
            product.setWholesalePrice(request.getWholesalePrice());

            // FIX: Update the image URL if it's sent from the frontend
            if (request.getProductImageUrl() != null && !request.getProductImageUrl().isEmpty()) {
                product.setProductImageUrl(request.getProductImageUrl());
            }

            if (request.getCategoryId() != null) {
                Category category = categoryRepository.findById(request.getCategoryId())
                        .orElseThrow(() -> new RuntimeException("Category not found"));
                product.setCategory(category);
            }
            return productRepository.save(product);
        }).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    // Delete Product
    @Transactional // Important: Ensures both deletes happen together
    public void deleteProduct(String productId) {
        //  Delete associated stock records first (Foreign Key Constraint)
        branchProductRepository.deleteByProduct_ProductId(productId);

        //  Now it is safe to delete the Master Product
        productRepository.deleteById(productId);
    }

    // Update Reorder Level for a specific Branch
    public BranchProduct updateReorderLevel(String inventoryId, Integer newLevel) {
        return branchProductRepository.findById(inventoryId).map(stock -> {
            stock.setReorderLevel(newLevel);
            return branchProductRepository.save(stock);
        }).orElseThrow(() -> new RuntimeException("Inventory record not found"));
    }

    @Transactional
    public void deductStock(String branchId, String productId, int quantityToDeduct) {
        // 1. Find the Stock Record (BranchProduct), NOT the Master Product
        BranchProduct stock = branchProductRepository.findByBranch_BranchIdAndProduct_ProductId(branchId, productId)
                .orElseThrow(() -> new RuntimeException("Product not found in this branch inventory!"));

        // 2. Check Quantity
        if (stock.getQuantity() < quantityToDeduct) {
            throw new RuntimeException("Not enough stock! Available: " + stock.getQuantity());
        }

        // 3. Deduct & Save
        stock.setQuantity(stock.getQuantity() - quantityToDeduct);
        branchProductRepository.save(stock);
    }
}