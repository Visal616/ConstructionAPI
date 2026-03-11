package com.example.contructioninventoryapi.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name = "transfers")
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID transferId;

    private UUID fromBranchId;
    private UUID toBranchId;
    private String description;
    private String status;
    private LocalDateTime transferDate;

    private String createdBy;


    @OneToMany(mappedBy = "transfer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TransferDetail> transferDetails;
}