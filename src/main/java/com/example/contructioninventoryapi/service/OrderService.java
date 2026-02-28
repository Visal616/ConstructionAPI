//package com.example.contructioninventoryapi.service;
//
//import com.example.contructioninventoryapi.entity.*;
//import com.example.contructioninventoryapi.repository.*;
//import jakarta.transaction.Transactional;
//import org.springframework.stereotype.Service;
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//@Service
//public class OrderService {
//
//    private final OrderRepository orderRepository;
//    private final ProductRepository productRepository;
//    private final OrderDetailRepository orderDetailRepository;
//    private final BranchProductRepository branchProductRepository; // 1. Inject this
//
//    public OrderService(OrderRepository orderRepository,
//                        ProductRepository productRepository,
//                        OrderDetailRepository orderDetailRepository,
//                        BranchProductRepository branchProductRepository) {
//        this.orderRepository = orderRepository;
//        this.productRepository = productRepository;
//        this.orderDetailRepository = orderDetailRepository;
//        this.branchProductRepository = branchProductRepository;
//    }
//
//    @Transactional
//    public Order createOrder(Order order) {
//        // Validation: Ensure Order has a Branch
//        if (order.getBranch() == null) {
//            throw new RuntimeException("Order must be assigned to a Branch.");
//        }
//
//        // 1. Setup Order Basic Info
//        order.setOrderId(UUID.randomUUID().toString());
//        order.setOrderDate(LocalDateTime.now());
//        order.setStatus("Completed");
//
//        // 2. Save the main Order
//        Order savedOrder = orderRepository.save(order);
//
//        // 3. Process Details & Update Branch Stock
//        if (order.getOrderDetails() != null) {
//            for (OrderDetail detail : order.getOrderDetails()) {
//                // Link detail to order
//                detail.setDetailId(UUID.randomUUID().toString());
//                detail.setOrder(savedOrder);
//
//                // Fetch Product (Just to verify it exists)
//                Product product = productRepository.findById(detail.getProduct().getProductId())
//                        .orElseThrow(() -> new RuntimeException("Product not found"));
//
//                // --- NEW STOCK LOGIC ---
//                // Find the inventory record for THIS Branch and THIS Product
//                BranchProduct inventory = branchProductRepository.findByBranchAndProduct(order.getBranch(), product)
//                        .orElseThrow(() -> new RuntimeException("Product is not available at this branch."));
//
//                // Check Branch Stock
//                if (inventory.getQuantity() < detail.getQuantity()) {
//                    throw new RuntimeException("Not enough stock at branch: " + order.getBranch().getBranchName());
//                }
//
//                // Decrease Stock at Branch Level
//                inventory.setQuantity(inventory.getQuantity() - detail.getQuantity());
//                branchProductRepository.save(inventory);
//                // -----------------------
//
//                // Save Detail
//                orderDetailRepository.save(detail);
//            }
//        }
//
//        return savedOrder;
//    }
//}