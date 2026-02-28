package com.example.contructioninventoryapi.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserResponse {
    private String userId;
    private String fullName;
    private String email;
    private String role;
    private boolean twoFactorEnabled;
    private String profileImageUrl;

    private CompanyInfo company;
    private List<BranchInfo> branches;

    @Data
    public static class CompanyInfo {
        private String companyId;
        private String companyName;
        private String address;
        private String contactNumber;
        private String email;
    }

    @Data
    public static class BranchInfo {
        private String branchId;
        private String branchName;
        private String location;
        private String contactNumber;
    }
}