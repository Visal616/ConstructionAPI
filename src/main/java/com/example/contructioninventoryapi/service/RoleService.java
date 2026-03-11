package com.example.contructioninventoryapi.service;

import com.example.contructioninventoryapi.entity.Role;
import java.util.List;

public interface RoleService {
    Role createRole(Role role);
    List<Role> getAllRoles();
    Role getRoleById(Integer id);
    Role updateRole(Integer id, Role role);
    void deleteRole(Integer id);
}