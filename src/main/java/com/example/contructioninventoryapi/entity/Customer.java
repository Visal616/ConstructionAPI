package com.example.contructioninventoryapi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String customerId;

    @Column(nullable = false)
    private String customerName;

    private String contactName;
    private String phone;
    private String email;
    private String address;

    @Column(name = "company_id", nullable = false)
    private String companyId;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}