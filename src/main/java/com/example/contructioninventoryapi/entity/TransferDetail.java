package com.example.contructioninventoryapi.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.UUID;

@Entity
@Data
@Table(name = "transfer_details")
public class TransferDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID transferDetailId;

    @ManyToOne
    @JoinColumn(name = "transfer_id")
    @ToString.Exclude
    private Transfer transfer;

    private String productId;
    private int quantity;
}