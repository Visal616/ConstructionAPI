package com.example.contructioninventoryapi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Report {
    @Id
    @Column(name = "report_id")
    private String reportId;

    @ManyToOne
    @JoinColumn(name = "generated_by")
    private User generatedBy;

    @Column(name = "report_type")
    private String reportType;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "file_path")
    private String filePath;
}