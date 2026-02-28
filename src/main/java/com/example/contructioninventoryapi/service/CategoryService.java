package com.example.contructioninventoryapi.service;

import com.example.contructioninventoryapi.entity.Category;
import com.example.contructioninventoryapi.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class CategoryService {
    private final CategoryRepository repo;
    public CategoryService(CategoryRepository repo) { this.repo = repo; }

    public List<Category> getAll() { return repo.findAll(); }

    public Category save(Category c) {
        if(c.getCategoryId() == null) c.setCategoryId(UUID.randomUUID().toString());
        return repo.save(c);
    }

    public void delete(String id) { repo.deleteById(id); }
}