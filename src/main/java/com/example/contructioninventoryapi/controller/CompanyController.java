package com.example.contructioninventoryapi.controller;

import com.example.contructioninventoryapi.entity.Company;
import com.example.contructioninventoryapi.service.CompanyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService service;

    public CompanyController(CompanyService service) {
        this.service = service;
    }

    @GetMapping
    public List<Company> getAll() {
        return service.getAll(); // Now this will work!
    }
}