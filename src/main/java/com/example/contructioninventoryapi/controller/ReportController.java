package com.example.contructioninventoryapi.controller;

import com.example.contructioninventoryapi.entity.Report;
import com.example.contructioninventoryapi.service.ReportService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService service;
    public ReportController(ReportService service) { this.service = service; }

    @GetMapping
    public List<Report> getAll() { return service.getAll(); }

    @PostMapping
    public Report create(@RequestBody Report r) { return service.create(r); }
}