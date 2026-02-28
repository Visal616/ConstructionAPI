package com.example.contructioninventoryapi.controller;

import com.example.contructioninventoryapi.entity.Role;
import com.example.contructioninventoryapi.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public List<Role> getAll() {
        return roleService.getAll();
    }

    // ✅ Change Long to String
    @GetMapping("/{id}")
    public ResponseEntity<Role> getById(@PathVariable String id) {
        return roleService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Role create(@RequestBody Role role) {
        return roleService.create(role);
    }

    // ✅ Change Long to String
    @PutMapping("/{id}")
    public ResponseEntity<Role> update(@PathVariable String id, @RequestBody Role role) {
        try {
            return ResponseEntity.ok(roleService.update(id, role));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ✅ Change Long to String
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        roleService.delete(id);
        return ResponseEntity.ok("Role deleted successfully");
    }
}