package com.example.contructioninventoryapi.repository;

import com.example.contructioninventoryapi.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, UUID> {
    List<Transfer> findByFromBranchIdOrToBranchIdOrderByTransferDateDesc(UUID fromBranchId, UUID toBranchId);
}