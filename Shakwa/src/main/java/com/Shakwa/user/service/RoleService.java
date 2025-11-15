package com.Shakwa.user.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Shakwa.user.dto.RoleRequestDTO;
import com.Shakwa.user.entity.Permission;
import com.Shakwa.user.entity.Role;
import com.Shakwa.user.repository.PermissionRepository;
import com.Shakwa.user.repository.RoleRepository;
import com.Shakwa.utils.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Role getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + id));
    }

    @Transactional
    public Role createRole(RoleRequestDTO requestDTO) {
        if (roleRepository.existsByName(requestDTO.getName())) {
            throw new IllegalArgumentException("Role with name " + requestDTO.getName() + " already exists");
        }
        Role roleEntity = new Role();
        roleEntity.setName(requestDTO.getName());
        roleEntity.setDescription(requestDTO.getDescription());
        roleEntity.setActive(requestDTO.getIsActive());
        roleEntity.setPermissions(
                requestDTO.getPermissionIds()
                        .stream()
                        .map(id -> permissionRepository.findById(id)
                                .orElseThrow(
                                        () -> new EntityNotFoundException("Permission not found with id: " + id)))
                .collect(Collectors.toSet())
        );
        return roleRepository.save(roleEntity);
    }

    public List<String> getPermissionsByRoleId(Long id) {
        return roleRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Role not exist with Id: " + id)
            ).getPermissions().stream().map(Permission::getName).collect(Collectors.toList());
    }

    @Transactional
    public Role updateRole(Long id, RoleRequestDTO roleDetails) {
        Role role = getRoleById(id);
        
        if (role.isSystem()) {
            throw new IllegalStateException("Cannot modify system role: " + role.getName());
        }
        
        if (!role.getName().equals(roleDetails.getName()) && 
            roleRepository.existsByName(roleDetails.getName())) {
            throw new IllegalArgumentException("Role with name " + roleDetails.getName() + " already exists");
        }
        
        role.setName(roleDetails.getName());
        role.setDescription(roleDetails.getDescription());
        role.setActive(roleDetails.getIsActive());
        
        return roleRepository.save(role);
    }

    @Transactional
    public void deleteRole(Long id) {
        Role role = getRoleById(id);
        
        if (role.isSystem()) {
            throw new IllegalStateException("Cannot delete system role: " + role.getName());
        }
        
        roleRepository.delete(role);
    }

    @Transactional
    public Role updateRolePermissions(Long roleId, Set<Long> permissionIds) {
        Role role = getRoleById(roleId);
        
        if (role.isSystem()) {
            throw new IllegalStateException("Cannot modify permissions of system role: " + role.getName());
        }
        
        Set<Permission> permissions = permissionIds.stream()
                .map(id -> permissionRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Permission not found with id: " + id)))
                .collect(Collectors.toSet());
        
        role.setPermissions(permissions);
        return roleRepository.save(role);
    }

    public Set<Permission> getDefaultPermissionsForRole(String roleName) {
        // Map role names to permission names
        Set<String> permissionNames = switch (roleName) {
            case "PHARMACY_MANAGER" -> Set.of(
                "USER_MANAGE", "EMPLOYEE_MANAGE", "PRODUCT_MANAGE", "SALE_MANAGE", "PURCHASE_MANAGE", "INVENTORY_VIEW", "INVENTORY_MANAGE", "REPORT_VIEW", "PERMISSION_MANAGE"
            );
            case "PHARMACIST" -> Set.of(
                "PRODUCT_MANAGE", "SALE_MANAGE", "PURCHASE_MANAGE", "INVENTORY_VIEW", "INVENTORY_MANAGE", "REPORT_VIEW"
            );
            case "TRAINEE" -> Set.of(
                "SALE_MANAGE", "INVENTORY_VIEW"
            );
            case "SYSTEM_ADMIN" -> Set.of(
                "USER_MANAGE", "EMPLOYEE_MANAGE", "PRODUCT_MANAGE", "SALE_MANAGE", "PURCHASE_MANAGE", "INVENTORY_VIEW", "INVENTORY_MANAGE", "REPORT_VIEW", "PERMISSION_MANAGE"
            );
            default -> Set.of();
        };
        return permissionNames.stream()
            .map(permissionRepository::findByName)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toSet());
    }

    /**
     * Get roles available for employees (excludes PLATFORM_ADMIN)
     * @return List of roles that can be assigned to employees (SUPERVISOR and VIEWER)
     */
    public List<Role> getEmployeeRoles() {
        return roleRepository.findAll().stream()
                .filter(role -> role.getName().equals("SUPERVISOR") || role.getName().equals("VIEWER"))
                .filter(Role::isActive)
                .collect(Collectors.toList());
    }
} 