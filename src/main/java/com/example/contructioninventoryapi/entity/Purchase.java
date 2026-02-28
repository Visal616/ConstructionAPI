package com.example.contructioninventoryapi.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchases")
@Data
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "purchase_id")
    private String purchaseId;

    @Column(name = "purchase_date")
    private LocalDateTime purchaseDate = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(name = "total_cost")
    private BigDecimal totalCost;

    @Column(name = "branch_id")
    private String branchId;

    @Column(name = "description")
    private String description;

    @Column(name = "status")
    private String status;      // "Pending", "Received"

    @ToString.Exclude
    @Column(name = "invoice_url")
    private String invoiceUrl;

    @Column(name = "received_date")
    private LocalDateTime receivedDate;

    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference // This pairs with @JsonBackReference in PurchaseDetail to prevent loops
    private List<PurchaseDetail> purchaseDetails;


    @Column(name = "payment_status", length = 50)
    private String paymentStatus = "Unpaid"; // Unpaid, Partial, Paid

    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PurchasePayment> payments = new ArrayList<>();

    public BigDecimal getPaidAmount() {
        return payments.stream()
                .map(PurchasePayment::getAmountPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}