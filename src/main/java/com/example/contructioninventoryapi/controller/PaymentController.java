package com.example.contructioninventoryapi.controller;

import com.example.contructioninventoryapi.entity.Payment;
import com.example.contructioninventoryapi.service.PaymentService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService service;
    public PaymentController(PaymentService service) { this.service = service; }

    @GetMapping
    public List<Payment> getAll() { return service.getAll(); }

    @PostMapping
    public Payment create(@RequestBody Payment p) { return service.save(p); }
}