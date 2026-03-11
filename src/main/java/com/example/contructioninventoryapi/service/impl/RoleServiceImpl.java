package com.example.contructioninventoryapi.service.impl;

import com.example.contructioninventoryapi.entity.Role;
import com.example.contructioninventoryapi.repository.RoleRepository;
import com.example.contructioninventoryapi.service.RoleService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    // Constructor-based dependency injection
    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Role createRole(Role role) {
        return roleRepository.save(role);
    }

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    public Role getRoleById(Integer id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + id));
    }

    @Override
    public Role updateRole(Integer id, Role roleDetails) {
        Role existingRole = getRoleById(id); // Throws exception if not found

        // Update fields
        existingRole.setRoleName(roleDetails.getRoleName());
        existingRole.setDescription(roleDetails.getDescription());

        return roleRepository.save(existingRole);
    }

    @Override
    public void deleteRole(Integer id) {
        Role existingRole = getRoleById(id);
        roleRepository.delete(existingRole);
    }
}