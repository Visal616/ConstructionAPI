package com.example.contructioninventoryapi.service;

import com.example.contructioninventoryapi.entity.Payment;
import com.example.contructioninventoryapi.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {
    private final PaymentRepository repository;
    public PaymentService(PaymentRepository repository) { this.repository = repository; }

    public List<Payment> getAll() { return repository.findAll(); }
    public Payment save(Payment p) {
        if(p.getPaymentId() == null) p.setPaymentId(UUID.randomUUID().toString());
        p.setPaymentDate(LocalDateTime.now());
        return repository.save(p);
    }
}