package com.example.contructioninventoryapi.controller;

import com.example.contructioninventoryapi.entity.Customer;
import com.example.contructioninventoryapi.repository.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "http://localhost:5173")
public class CustomerController {

    private final CustomerRepository customerRepository;

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping
    public Page<Customer> getCustomers(
            @RequestParam String companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "customerName") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        if (search != null && !search.isEmpty()) {
            return customerRepository.searchCustomers(companyId, search, pageable);
        }
        return customerRepository.findByCompanyId(companyId, pageable);
    }
    
    @PostMapping
    public Customer createCustomer(@RequestBody Customer customer) {
        if (customer.getCustomerId() != null && customer.getCustomerId().trim().isEmpty()) {
            customer.setCustomerId(null);
        }

        return customerRepository.save(customer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable String id, @RequestBody Customer details) {
        return customerRepository.findById(id).map(c -> {
            c.setCustomerName(details.getCustomerName());
            c.setContactName(details.getContactName());
            c.setPhone(details.getPhone());
            c.setEmail(details.getEmail());
            c.setAddress(details.getAddress());
            return ResponseEntity.ok(customerRepository.save(c));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable String id) {
        customerRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}