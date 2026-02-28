package com.example.contructioninventoryapi.controller;

import com.example.contructioninventoryapi.dto.SalesRequest;
import com.example.contructioninventoryapi.entity.Invoice; // Add this import
import com.example.contructioninventoryapi.entity.Order;
import com.example.contructioninventoryapi.service.SalesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map; // Add this import

@RestController
@RequestMapping("/api/sales")
@CrossOrigin(origins = "http://localhost:5173")
public class SalesController {

    @Autowired private SalesService salesService;

    @PostMapping
    public ResponseEntity<Order> createSale(@RequestBody SalesRequest request) {
        return ResponseEntity.ok(salesService.createSale(request));
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllSales(@RequestParam String branchId) {
        return ResponseEntity.ok(salesService.getAllSales(branchId));
    }

    // === ADD THIS NEW ENDPOINT FOR PAYMENTS ===
    @PostMapping("/payments/{invoiceId}")
    public ResponseEntity<Invoice> addPayment(
            @PathVariable String invoiceId,
            @RequestBody Map<String, Object> payload) {

        // Extract the payment data from the JSON payload
        BigDecimal amountToPay = new BigDecimal(payload.get("amountToPay").toString());
        String paymentMethod = payload.get("paymentMethod").toString();
        String transactionRef = payload.get("transactionRef") != null ? payload.get("transactionRef").toString() : null;

        Invoice updatedInvoice = salesService.addPayment(invoiceId, amountToPay, paymentMethod, transactionRef);
        return ResponseEntity.ok(updatedInvoice);
    }
}