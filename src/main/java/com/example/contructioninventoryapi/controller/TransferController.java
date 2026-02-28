package com.example.contructioninventoryapi.controller;

import com.example.contructioninventoryapi.dto.TransferRequestDTO;
import com.example.contructioninventoryapi.service.TransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/transfers")
@CrossOrigin(origins = "*")
public class TransferController {

    @Autowired
    private TransferService transferService;

    // GET: http://localhost:8080/api/transfers/branch/{branchId}
    @GetMapping("/branch/{branchId}")
    public ResponseEntity<?> getTransfersByBranch(@PathVariable UUID branchId) {
        try {
            // Get all transfers where the branch is either the sender OR the receiver
            return ResponseEntity.ok(transferService.getTransfersByBranch(branchId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // POST: http://localhost:8080/api/transfers
    @PostMapping
    public ResponseEntity<?> createTransfer(@RequestBody TransferRequestDTO request) {
        try {
            return ResponseEntity.ok(transferService.createTransfer(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PUT: http://localhost:8080/api/transfers/{id}/status
    // Optional: Restrict approval/rejection to Admins only
    // @PreAuthorize("hasRole('ADMIN')") 
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateTransferStatus(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        try {
            String newStatus = body.get("status");
            return ResponseEntity.ok(transferService.updateStatus(id, newStatus));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}