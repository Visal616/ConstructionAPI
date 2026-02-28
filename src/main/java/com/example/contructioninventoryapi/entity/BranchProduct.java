package com.example.contructioninventoryapi.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Branch_Products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BranchProduct {
    @Id
    @Column(name = "inventory_id")
    private String inventoryId; // UUID

    @Column(name = "quantity")
    private Integer quantity = 0; // Stock for this specific branch

    @Column(name = "reorder_level")
    private Integer reorderLevel = 10; // Alert specific to this branch

    // Relationships
    @ManyToOne
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}