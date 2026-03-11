package com.example.contructioninventoryapi.service;

import com.example.contructioninventoryapi.dto.TransferRequestDTO;
import com.example.contructioninventoryapi.dto.TransferItemDTO;
import com.example.contructioninventoryapi.entity.Branch;
import com.example.contructioninventoryapi.entity.BranchProduct;
import com.example.contructioninventoryapi.entity.Product;
import com.example.contructioninventoryapi.entity.Transfer;
import com.example.contructioninventoryapi.entity.TransferDetail;

import com.example.contructioninventoryapi.repository.BranchRepository;
import com.example.contructioninventoryapi.repository.BranchProductRepository;
import com.example.contructioninventoryapi.repository.ProductRepository;
import com.example.contructioninventoryapi.repository.TransferRepository;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransferService {

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private BranchProductRepository inventoryRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<Transfer> getTransfersByBranch(UUID branchId) {
        // Fetch where fromBranch = branchId OR toBranch = branchId
        return transferRepository.findByFromBranchIdOrToBranchIdOrderByTransferDateDesc(branchId, branchId);
    }

    @Transactional
    public Transfer createTransfer(TransferRequestDTO request) {
        Transfer transfer = new Transfer();
        transfer.setFromBranchId(request.getFromBranchId());
        transfer.setToBranchId(request.getToBranchId());
        transfer.setDescription(request.getDescription());
        transfer.setStatus("Pending"); // Initial status

        if (request.getCreatedBy() != null && !request.getCreatedBy().trim().isEmpty()) {
            transfer.setCreatedBy(request.getCreatedBy());
        } else {
            transfer.setCreatedBy("System/Admin"); // Fallback if no user is provided
        }

        transfer.setTransferDate(LocalDateTime.now());

        List<TransferDetail> details = new ArrayList<>();

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (TransferItemDTO itemDto : request.getItems()) {
                TransferDetail detail = new TransferDetail();
                detail.setProductId(itemDto.getProductId());
                detail.setQuantity(itemDto.getQuantity());

                // CRITICAL: Link this detail back to the parent transfer
                detail.setTransfer(transfer);

                details.add(detail);
            }
        }

        // Attach the list of details to the main transfer object
        transfer.setTransferDetails(details);

        // Saving the transfer will now cascade and save all the details too!
        return transferRepository.save(transfer);
    }

    @Transactional
    public Transfer updateStatus(UUID transferId, String newStatus) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new RuntimeException("Transfer not found"));

        String currentStatus = transfer.getStatus();

        // 1. ADMIN APPROVES -> Deduct from Branch A
        if ("In Transit".equals(newStatus) && "Pending".equals(currentStatus)) {
            for (TransferDetail item : transfer.getTransferDetails()) {
                deductStock(transfer.getFromBranchId(), item.getProductId(), item.getQuantity());
            }
        }
        // 2. DESTINATION RECEIVES -> Add to Branch B
        else if ("Completed".equals(newStatus) && "In Transit".equals(currentStatus)) {
            for (TransferDetail item : transfer.getTransferDetails()) {
                addStock(transfer.getToBranchId(), item.getProductId(), item.getQuantity());
            }
        }
        // 3. DESTINATION REJECTS DELIVERY -> Return stock to Branch A
        else if ("Rejected".equals(newStatus) && "In Transit".equals(currentStatus)) {
            for (TransferDetail item : transfer.getTransferDetails()) {
                // Notice we use 'getFromBranchId' here to put it back where it came from!
                addStock(transfer.getFromBranchId(), item.getProductId(), item.getQuantity());
            }
        }

        transfer.setStatus(newStatus);
        return transferRepository.save(transfer);
    }

    // ==========================================
    // INVENTORY MANAGEMENT HELPER METHODS
    // ==========================================

    private void deductStock(UUID branchId, String productId, int qty) {
        String branchIdStr = branchId.toString();

        // 1. Find the product in the sending branch's inventory
        BranchProduct inventory = inventoryRepository.findByBranch_BranchIdAndProduct_ProductId(branchIdStr, productId)
                .orElseThrow(() -> new RuntimeException("Product not found in source branch inventory!"));

        // 2. Check if they have enough physical stock
        if (inventory.getQuantity() < qty) {
            throw new RuntimeException("Insufficient stock in source branch for product ID: " + productId);
        }

        // 3. Deduct and save
        inventory.setQuantity(inventory.getQuantity() - qty);
        inventoryRepository.save(inventory);
    }

    private void addStock(UUID branchId, String productId, int qty) {
        String branchIdStr = branchId.toString();

        // 1. Try to find the product in the receiving branch's inventory
        Optional<BranchProduct> inventoryOpt = inventoryRepository.findByBranch_BranchIdAndProduct_ProductId(branchIdStr, productId);

        if (inventoryOpt.isPresent()) {
            // 2a. If they already have it, just add the quantity
            BranchProduct inventory = inventoryOpt.get();
            inventory.setQuantity(inventory.getQuantity() + qty);
            inventoryRepository.save(inventory);
        } else {
            // 2b. If the receiving branch has never had this product before, create a new record!
            BranchProduct newInventory = new BranchProduct();
            newInventory.setInventoryId(UUID.randomUUID().toString());
            newInventory.setQuantity(qty);
            newInventory.setReorderLevel(10); // Default fallback

            // Fetch the actual Branch entity from the database
            Branch branch = branchRepository.findById(branchIdStr)
                    .orElseThrow(() -> new RuntimeException("Destination branch not found in database!"));
            newInventory.setBranch(branch);

            // Fetch the actual Product entity from the database
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found in database!"));
            newInventory.setProduct(product);

            inventoryRepository.save(newInventory);
        }
    }
}