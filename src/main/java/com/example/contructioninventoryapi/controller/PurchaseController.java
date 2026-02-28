package com.example.contructioninventoryapi.controller;

import com.example.contructioninventoryapi.dto.PurchasePaymentRequest;
import com.example.contructioninventoryapi.dto.PurchaseRequest;
import com.example.contructioninventoryapi.entity.Purchase;
import com.example.contructioninventoryapi.service.FileStorageService;
import com.example.contructioninventoryapi.service.PurchaseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/purchases")
@CrossOrigin(origins = "http://localhost:5173")
public class PurchaseController {

    @Autowired private PurchaseService purchaseService;
    @Autowired private FileStorageService fileStorageService;

    // 1. CREATE PURCHASE
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Purchase> createPurchase(
            @RequestPart("data") MultipartFile jsonFile,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {

        // 1. Decode JSON (Safe UTF-8)
        String jsonString = new String(jsonFile.getBytes(), StandardCharsets.UTF_8);

        // 2. Map to Object
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        // Prevent crash on unknown fields
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        PurchaseRequest request = mapper.readValue(jsonString, PurchaseRequest.class);

        // 3. Upload File
        String invoiceUrl = null;
        if (file != null && !file.isEmpty()) {
            invoiceUrl = fileStorageService.storeFile(file);
        }

        // 4. Save
        Purchase newPurchase = purchaseService.createPurchase(request, invoiceUrl);
        return ResponseEntity.ok(newPurchase);
    }

    // 2. LIST PURCHASES
    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<Purchase>> getPurchases(@PathVariable String branchId) {
        return ResponseEntity.ok(purchaseService.getPurchasesByBranch(branchId));
    }

    // 3. UPDATE STATUS
    @PutMapping("/{id}/status")
    public ResponseEntity<Purchase> updateStatus(@PathVariable String id, @RequestParam String status) {
        return ResponseEntity.ok(purchaseService.updateStatus(id, status));
    }

    // 4. UPLOAD INVOICE
    @PostMapping("/{id}/invoice")
    public ResponseEntity<Purchase> uploadInvoice(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        // 1. Store the file
        String fileName = fileStorageService.storeFile(file);

        // 2. Update the database
        Purchase updatedPurchase = purchaseService.updateInvoice(id, fileName);

        return ResponseEntity.ok(updatedPurchase);
    }

    // 5. ADD PAYMENT (ថ្មី)
    @PostMapping("/{id}/payments")
    public ResponseEntity<Purchase> addPayment(
            @PathVariable String id,
            @RequestBody PurchasePaymentRequest request
    ) {
        try {
            Purchase updatedPurchase = purchaseService.addPayment(id, request);
            return ResponseEntity.ok(updatedPurchase);
        } catch (Exception e) {
            // អ្នកអាចប្តូរ Error Handling នេះទៅតាម Standard របស់ Project អ្នកបាន
            return ResponseEntity.badRequest().build();
        }
    }
}