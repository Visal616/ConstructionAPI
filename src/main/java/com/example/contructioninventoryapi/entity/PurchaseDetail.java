package com.example.contructioninventoryapi.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "purchase_item_id", length = 50)
    private String purchaseItemId;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "unit_cost")
    private BigDecimal unitCost;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "purchase_id")
    @JsonBackReference
    private Purchase purchase;
}