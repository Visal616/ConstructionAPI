package com.example.contructioninventoryapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "invoices")
@Data
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "invoice_id")
    private String invoiceId;

    @Column(name = "invoice_date")
    private LocalDateTime invoiceDate = LocalDateTime.now();

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "grand_total")
    private BigDecimal grandTotal;

    @Column(name = "tax_amount")
    private BigDecimal taxAmount = BigDecimal.ZERO;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "branch_id")
    @JsonIgnoreProperties({"company.branches", "company.users"})
    private Branch branch;

    @OneToOne
    @JoinColumn(name = "order_id")
    @JsonIgnoreProperties("invoice")
    @JsonIgnore
    private Order order;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnoreProperties("invoice")
    private List<Payment> payments;

    public BigDecimal getTotalPaid() {
        if (payments == null) return BigDecimal.ZERO;
        return payments.stream()
                .map(Payment::getAmountPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}