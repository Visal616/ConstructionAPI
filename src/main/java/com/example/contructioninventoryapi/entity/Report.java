package com.example.contructioninventoryapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports") // Changed to lowercase 'reports' to prevent database case-sensitivity issues
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "report_id")
    private String reportId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "generated_by")
    @JsonIgnoreProperties({"password", "branches", "reports"})
    private User generatedBy;

    @Column(name = "report_type")
    private String reportType; // e.g., "Sales", "Inventory", "Financial"

    @Column(name = "generated_at")
    private LocalDateTime generatedAt = LocalDateTime.now();

    @Column(name = "file_path")
    private String filePath;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "branch_id")
    @JsonIgnoreProperties({"company", "users", "reports"}) // Prevent JSON infinite loops
    private Branch branch;
}