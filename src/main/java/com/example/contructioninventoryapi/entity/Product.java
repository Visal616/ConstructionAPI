package com.example.contructioninventoryapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "Products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @Column(name = "product_id")
    private String productId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

//    Link product to the Company (Global Definition)
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "company_id")
//    @JsonIgnore
//    private Company company;

    @Column(name = "company_id", nullable = false)
    private String companyId;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "cost_price", nullable = false)
    private BigDecimal costPrice;

    @Column(name = "base_unit", nullable = false)
    private String baseUnit;

    @Column(name = "wholesale_price")
    private BigDecimal wholesalePrice;

    @Column(name = "purchase_unit")
    private String purchaseUnit;

    @Column(name = "conversion_rate", nullable = false)
    private Integer conversionRate = 1;

    @Column(name = "product_image_url")
    private String productImageUrl;
}