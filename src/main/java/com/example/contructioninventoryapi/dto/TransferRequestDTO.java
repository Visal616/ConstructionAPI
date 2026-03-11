package com.example.contructioninventoryapi.dto;

import com.example.contructioninventoryapi.dto.TransferItemDTO;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Data
public class TransferRequestDTO {
    private UUID fromBranchId;
    private UUID toBranchId;
    private String createdBy;
    private String description;
    private List<TransferItemDTO> items;
}
