package com.example.contructioninventoryapi.controller;

import com.example.contructioninventoryapi.entity.Report;
import com.example.contructioninventoryapi.service.ReportService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService service;
    public ReportController(ReportService service) { this.service = service; }

    // UPDATED: Now accepts branchId to filter history
    @GetMapping
    public List<Report> getAll(@RequestParam(required = false) String branchId) {
        return service.getAll(branchId);
    }

    @PostMapping("/generate")
    public Report generateReport(
            @RequestParam String userId,
            @RequestParam String reportType,
            @RequestParam String format,
            @RequestParam(required = false) String branchId,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) throws Exception {
        return service.generateManagementReport(userId, reportType, format, branchId, categoryId, startDate, endDate);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadReport(@PathVariable String id) throws Exception {
        Report report = service.getById(id);
        Path path = Paths.get(report.getFilePath());
        Resource resource = new UrlResource(path.toUri());

        String contentType = path.toString().endsWith(".pdf") ? "application/pdf" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + path.getFileName().toString() + "\"")
                .body(resource);
    }
}