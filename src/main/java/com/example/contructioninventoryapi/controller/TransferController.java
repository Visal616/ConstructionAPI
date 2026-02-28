package com.example.contructioninventoryapi.controller;

import com.example.contructioninventoryapi.dto.TransferRequestDTO;
import com.example.contructioninventoryapi.service.TransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/transfers")
@CrossOrigin(origins = "*")
public class TransferController {

    @Autowired
    private TransferService transferService;

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<?> getTransfersByBranch(@PathVariable UUID branchId) {
        try {
            return ResponseEntity.ok(transferService.getTransfersByBranch(branchId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createTransfer(@RequestBody TransferRequestDTO request) {
        try {
            return ResponseEntity.ok(transferService.createTransfer(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // IMPORTANT: Changed to @RequestParam to match React's 'params: { status }'
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateTransferStatus(
            @PathVariable UUID id,
            @RequestParam String status) {
        try {
            return ResponseEntity.ok(transferService.updateStatus(id, status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}