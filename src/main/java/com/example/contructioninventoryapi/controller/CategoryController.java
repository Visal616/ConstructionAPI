package com.example.contructioninventoryapi.controller;

import com.example.contructioninventoryapi.entity.Category;
import com.example.contructioninventoryapi.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "http://localhost:5173")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // GET ALL (With Search, Sort, Pagination)
    @GetMapping
    public Page<Category> getCategories(
            @RequestParam String companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "categoryName") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        if (search != null && !search.isEmpty()) {
            return categoryRepository.searchCategories(companyId, search, pageable);
        }

        // FIXED: Calling the updated clean method name!
        return categoryRepository.findByCompanyId(companyId, pageable);
    }

    // CREATE
    @PostMapping
    public Category createCategory(@RequestBody Category category) {
        return categoryRepository.save(category);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable String id, @RequestBody Category details) {
        return categoryRepository.findById(id).map(c -> {
            c.setCategoryName(details.getCategoryName());
            c.setDescription(details.getDescription());
            return ResponseEntity.ok(categoryRepository.save(c));
        }).orElse(ResponseEntity.notFound().build());
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable String id) {
        categoryRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}