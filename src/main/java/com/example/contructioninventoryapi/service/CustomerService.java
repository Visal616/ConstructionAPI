package com.example.contructioninventoryapi.service;

import com.example.contructioninventoryapi.entity.Customer;
import com.example.contructioninventoryapi.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class CustomerService {
    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) { this.repository = repository; }

    public List<Customer> getAll() { return repository.findAll(); }

    public Customer save(Customer customer) {
        if(customer.getCustomerId() == null) customer.setCustomerId(UUID.randomUUID().toString());
        return repository.save(customer);
    }

    public void delete(String id) { repository.deleteById(id); }
}