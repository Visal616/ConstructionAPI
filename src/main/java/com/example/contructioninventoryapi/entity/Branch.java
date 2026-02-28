package com.example.contructioninventoryapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "branches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Branch {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "branch_id")
    private String branchId;

    @Column(name = "branch_name", nullable = false)
    private String branchName;

    private String location;

    @Column(name = "contact_number")
    private String contactNumber;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "company_id", nullable = false)
    @JsonIgnoreProperties({"branches", "users"})
    private Company company;

    @JsonIgnore
    @ManyToMany(mappedBy = "branches")
    private List<User> users;
}