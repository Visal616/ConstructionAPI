package com.example.contructioninventoryapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_id")
    private String paymentId;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "invoice_id")
    @JsonIgnoreProperties("payments")
    private Invoice invoice;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate = LocalDateTime.now();

    @Column(name = "amount_paid", nullable = false)
    private BigDecimal amountPaid;

    @Column(name = "payment_method") // "Cash", "ABA", "Bank"
    private String paymentMethod;

    @Column(name = "transaction_ref") // Optional: ABA Transaction ID
    private String transactionRef;
}
