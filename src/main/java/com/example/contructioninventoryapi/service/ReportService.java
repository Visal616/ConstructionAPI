package com.example.contructioninventoryapi.service;

import com.example.contructioninventoryapi.entity.Report;
import com.example.contructioninventoryapi.repository.ReportRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

@Service
public class ReportService {
    private final ReportRepository repo;
    public ReportService(ReportRepository repo) { this.repo = repo; }

    public List<Report> getAll() { return repo.findAll(); }

    public Report create(Report r) {
        r.setReportId(UUID.randomUUID().toString());
        r.setGeneratedAt(LocalDateTime.now());
        return repo.save(r);
    }
}