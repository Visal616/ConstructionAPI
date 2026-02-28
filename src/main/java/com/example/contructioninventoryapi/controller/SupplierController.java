package com.example.contructioninventoryapi.controller;

import com.example.contructioninventoryapi.entity.Supplier;
import com.example.contructioninventoryapi.repository.SupplierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/suppliers")
@CrossOrigin(origins = "http://localhost:5173")
public class SupplierController {

    private final SupplierRepository supplierRepository;

    public SupplierController(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    // GET ALL (With Search, Sort, Pagination)
    @GetMapping
    public Page<Supplier> getSuppliers(
            @RequestParam String companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "supplierName") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        if (search != null && !search.isEmpty()) {
            return supplierRepository.searchSuppliers(companyId, search, pageable);
        }
        return supplierRepository.findByCompanyId(companyId, pageable);
    }

    // CREATE
    @PostMapping
    public Supplier createSupplier(@RequestBody Supplier supplier) {
        return supplierRepository.save(supplier);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Supplier> updateSupplier(@PathVariable String id, @RequestBody Supplier details) {
        return supplierRepository.findById(id).map(s -> {
            s.setSupplierName(details.getSupplierName());
            s.setContactName(details.getContactName());
            s.setPhone(details.getPhone());
            s.setEmail(details.getEmail());
            s.setAddress(details.getAddress());
            return ResponseEntity.ok(supplierRepository.save(s));
        }).orElse(ResponseEntity.notFound().build());
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSupplier(@PathVariable String id) {
        supplierRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}