package com.example.contructioninventoryapi.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserRequest {
    private String email;
    private String fullName;
    private String password;
    private String phoneNumber;
    private boolean status;
    private String profileImageUrl;

    // IDs for relationships
    private String roleId;
    private String companyId;

    private List<String> branchIds;
}