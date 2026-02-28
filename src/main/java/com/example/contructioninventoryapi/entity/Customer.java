package com.example.contructioninventoryapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "Customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    @Id
    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    private String phone;
    private String email;
    private String address;

    @OneToMany(mappedBy = "customer")
    @JsonIgnore
    private List<Order> orders;
}
