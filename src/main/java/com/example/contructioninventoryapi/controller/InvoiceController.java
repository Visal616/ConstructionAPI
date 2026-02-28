package com.example.contructioninventoryapi.controller;

import com.example.contructioninventoryapi.entity.Invoice;
import com.example.contructioninventoryapi.service.InvoiceService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {
    private final InvoiceService service;
    public InvoiceController(InvoiceService service) { this.service = service; }

    @GetMapping
    public List<Invoice> getAll() { return service.getAll(); }

    @PostMapping
    public Invoice create(@RequestBody Invoice invoice) { return service.createInvoice(invoice); }
}