//package com.example.contructioninventoryapi.controller;
//
//import com.example.contructioninventoryapi.entity.Order;
//import com.example.contructioninventoryapi.service.OrderService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/orders")
//public class OrderController {
//
//    private final OrderService orderService;
//
//    public OrderController(OrderService orderService) {
//        this.orderService = orderService;
//    }
//
//    @PostMapping
//    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
//        return ResponseEntity.ok(orderService.createOrder(order));
//    }
//}