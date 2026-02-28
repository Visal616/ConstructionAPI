package com.example.contructioninventoryapi.controller;

import com.example.contructioninventoryapi.entity.Customer;
import com.example.contructioninventoryapi.service.CustomerService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerService service;
    public CustomerController(CustomerService service) { this.service = service; }

    @GetMapping
    public List<Customer> getAll() { return service.getAll(); }

    @PostMapping
    public Customer create(@RequestBody Customer c) { return service.save(c); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) { service.delete(id); }
}