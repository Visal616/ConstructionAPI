package com.example.contructioninventoryapi.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "suppliers")
@Data
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String supplierId;

    @Column(nullable = false)
    private String supplierName;

    private String contactName;
    private String phone;
    private String email;
    private String address;
    private String companyId;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}