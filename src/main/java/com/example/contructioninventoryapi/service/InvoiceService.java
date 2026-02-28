package com.example.contructioninventoryapi.service;

import com.example.contructioninventoryapi.entity.Invoice;
import com.example.contructioninventoryapi.repository.InvoiceRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class InvoiceService {
    private final InvoiceRepository repository;

    public InvoiceService(InvoiceRepository repository) { this.repository = repository; }

    public List<Invoice> getAll() { return repository.findAll(); }

    public Invoice createInvoice(Invoice invoice) {
        if(invoice.getInvoiceId() == null) invoice.setInvoiceId(UUID.randomUUID().toString());
        // Business logic: Calculate tax or validate totals could go here
        return repository.save(invoice);
    }

    public void delete(String id) { repository.deleteById(id); }
}