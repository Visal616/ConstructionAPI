package com.example.contructioninventoryapi.service;

import com.example.contructioninventoryapi.entity.Role;
import com.example.contructioninventoryapi.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<Role> getAll() {
        return roleRepository.findAll();
    }

    // ✅ Change Long to String
    public Optional<Role> getById(String id) {
        return roleRepository.findById(id);
    }

    public Role create(Role role) {
        // If ID is not set, you might want to generate one or let DB handle it
        // role.setRoleId(UUID.randomUUID().toString());
        return roleRepository.save(role);
    }

    // ✅ Change Long to String
    public Role update(String id, Role roleDetails) {
        return roleRepository.findById(id).map(role -> {
            role.setRoleName(roleDetails.getRoleName());
            return roleRepository.save(role);
        }).orElseThrow(() -> new RuntimeException("Role not found"));
    }

    // ✅ Change Long to String (This fixes your specific error)
    public void delete(String id) {
        roleRepository.deleteById(id);
    }
}