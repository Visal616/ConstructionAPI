package com.example.contructioninventoryapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "company_id")
    private String companyId;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    private String address;
    private String email;

    @Column(name = "contact_number")
    private String contactNumber;

    @Column(name = "register_date")
    private LocalDateTime registerDate;

    @OneToMany(mappedBy = "company")
    @JsonIgnore
    private List<User> users;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Branch> branches;
}