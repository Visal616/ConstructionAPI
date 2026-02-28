package com.example.contructioninventoryapi.service;

import com.example.contructioninventoryapi.dto.PurchasePaymentRequest;
import com.example.contructioninventoryapi.dto.PurchaseRequest;
import com.example.contructioninventoryapi.entity.*;
import com.example.contructioninventoryapi.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PurchaseService {

    @Autowired private PurchaseRepository purchaseRepository;
    @Autowired private PurchaseDetailRepository detailRepository;
    @Autowired private SupplierRepository supplierRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductService productService;


    @Autowired private PurchasePaymentRepository paymentRepository;

    @Transactional
    public Purchase createPurchase(PurchaseRequest request, String invoiceUrl) {

        // 1. Create Purchase Header
        Purchase purchase = new Purchase();
        purchase.setBranchId(request.getBranchId());
        purchase.setDescription(request.getDescription());
        purchase.setTotalCost(request.getTotalCost());
        purchase.setStatus(request.getStatus());
        purchase.setInvoiceUrl(invoiceUrl);
        purchase.setPurchaseDate(LocalDateTime.now());

        // កំណត់ស្ថានភាពបង់ប្រាក់បឋម
        purchase.setPaymentStatus(request.getPaymentStatus() != null ? request.getPaymentStatus() : "Unpaid");

        if ("Received".equalsIgnoreCase(request.getStatus())) {
            purchase.setReceivedDate(LocalDateTime.now());
        }

        // Link Supplier
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new RuntimeException("Supplier not found: " + request.getSupplierId()));
        purchase.setSupplier(supplier);

        // Save Header សិន ដើម្បីទទួលបាន ID
        Purchase savedPurchase = purchaseRepository.save(purchase);

        // --- 2. កត់ត្រាការបង់ប្រាក់ដំបូង (បើមាន) ---
        if (request.getAmountPaid() != null && request.getAmountPaid().compareTo(BigDecimal.ZERO) > 0) {
            PurchasePayment initialPayment = new PurchasePayment();
            initialPayment.setPurchase(savedPurchase);
            initialPayment.setAmountPaid(request.getAmountPaid());
            initialPayment.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "Cash");
            initialPayment.setPaymentDate(LocalDateTime.now());

            paymentRepository.save(initialPayment);
        }

        // 3. Save Details (Items)
        if (request.getItems() != null) {
            for (PurchaseRequest.PurchaseItemDto itemDto : request.getItems()) {
                PurchaseDetail detail = new PurchaseDetail();
                detail.setPurchase(savedPurchase);

                Product product = productRepository.findById(itemDto.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found: " + itemDto.getProductId()));
                detail.setProduct(product);

                detail.setQuantity(itemDto.getQuantity());
                detail.setUnitCost(itemDto.getUnitCost());

                detailRepository.save(detail);

                // 4. Update Stock if Received
                if ("Received".equalsIgnoreCase(request.getStatus())) {
                    productService.addStock(request.getBranchId(), product.getProductId(), itemDto.getQuantity());
                }
            }
        }

        return savedPurchase;
    }

    // --- បន្ថែម Method ថ្មីសម្រាប់ Add Payment ---
    @Transactional
    public Purchase addPayment(String purchaseId, PurchasePaymentRequest request) {
        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new RuntimeException("Purchase not found: " + purchaseId));

        // 1. បង្កើតកំណត់ត្រាបង់ប្រាក់ថ្មី
        PurchasePayment payment = new PurchasePayment();
        payment.setPurchase(purchase);
        payment.setAmountPaid(request.getAmountToPay());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setTransactionRef(request.getTransactionRef());
        payment.setPaymentDate(LocalDateTime.now());

        paymentRepository.save(payment);

        // 2. គណនាប្រាក់ដែលបានបង់រួចសរុប (បូកបញ្ចូលទាំងប្រាក់ទើបបង់ថ្មី)
        BigDecimal totalPaidSoFar = purchase.getPayments().stream()
                .map(PurchasePayment::getAmountPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(request.getAmountToPay());

        // 3. កែប្រែស្ថានភាពបង់ប្រាក់ដោយស្វ័យប្រវត្តិ
        if (totalPaidSoFar.compareTo(purchase.getTotalCost()) >= 0) {
            purchase.setPaymentStatus("Paid");
        } else if (totalPaidSoFar.compareTo(BigDecimal.ZERO) > 0) {
            purchase.setPaymentStatus("Partial");
        } else {
            purchase.setPaymentStatus("Unpaid");
        }

        return purchaseRepository.save(purchase);
    }

    public List<Purchase> getPurchasesByBranch(String branchId) {
        return purchaseRepository.findByBranchIdOrderByPurchaseDateDesc(branchId);
    }

    @Transactional
    public Purchase updateStatus(String id, String status) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase not found"));

        String oldStatus = purchase.getStatus();
        purchase.setStatus(status);

        if ("Pending".equalsIgnoreCase(oldStatus) && "Received".equalsIgnoreCase(status)) {
            List<PurchaseDetail> details = detailRepository.findByPurchase(purchase);

            for (PurchaseDetail item : details) {
                productService.addStock(purchase.getBranchId(), item.getProduct().getProductId(), item.getQuantity());
            }
            purchase.setReceivedDate(LocalDateTime.now());
        }

        return purchaseRepository.save(purchase);
    }

    @Transactional
    public Purchase updateInvoice(String purchaseId, String fileName) {
        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new RuntimeException("Purchase not found"));

        purchase.setInvoiceUrl(fileName);
        return purchaseRepository.save(purchase);
    }
}