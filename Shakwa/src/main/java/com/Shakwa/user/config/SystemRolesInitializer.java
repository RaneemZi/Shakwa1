package com.Shakwa.user.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.Shakwa.user.entity.Permission;
import com.Shakwa.user.entity.Role;
import com.Shakwa.user.repository.PermissionRepository;
import com.Shakwa.user.repository.RoleRepository;
import com.Shakwa.user.config.RoleConstants;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Order(1) // Run after database migration
public class SystemRolesInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(SystemRolesInitializer.class);

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Initializing system roles and permissions...");

        if (permissionRepository.count() == 0) {
            Map<String, Permission> permissions = createPermissions();

            if(roleRepository.count() == 0) {
                // Create system roles with their permissions
                createSystemRoles(permissions);
            }
        }
        else {
            log.info("System roles and permissions already exist.");
        }
        // Create permissions
        log.info("System roles and permissions initialized successfully");
    }

    private Map<String, Permission> createPermissions() {
        Map<String, Permission> permissions = new HashMap<>();
        
        // Define all governmentAgency system permissions
        List<Permission> permissionList = Arrays.asList(
            // User management
            createPermission("USER_CREATE", "Create users", "USER", "CREATE"),
            createPermission("USER_READ", "View users", "USER", "READ"),
            createPermission("USER_UPDATE", "Update users", "USER", "UPDATE"),
            createPermission("USER_DELETE", "Delete users", "USER", "DELETE"),

            // Employee management
            createPermission("EMPLOYEE_CREATE", "Create employees", "EMPLOYEE", "CREATE"),
            createPermission("EMPLOYEE_READ", "View employees", "EMPLOYEE", "READ"),
            createPermission("EMPLOYEE_UPDATE", "Update employees", "EMPLOYEE", "UPDATE"),
            createPermission("EMPLOYEE_DELETE", "Delete employees", "EMPLOYEE", "DELETE"),

            // GovernmentAgency management
            createPermission("PHARMACY_UPDATE", "Update governmentAgency info", "PHARMACY", "UPDATE"),
            createPermission("PHARMACY_READ", "View governmentAgency info", "PHARMACY", "READ"),

            // Product management
            createPermission("PRODUCT_CREATE", "Create products", "PRODUCT", "CREATE"),
            createPermission("PRODUCT_READ", "View products", "PRODUCT", "READ"),
            createPermission("PRODUCT_UPDATE", "Update products", "PRODUCT", "UPDATE"),
            createPermission("PRODUCT_DELETE", "Delete products", "PRODUCT", "DELETE"),

            // Inventory management
            createPermission("INVENTORY_READ", "View inventory", "INVENTORY", "READ"),
            createPermission("INVENTORY_UPDATE", "Update inventory", "INVENTORY", "UPDATE"),

            // Sales management
            createPermission("SALE_CREATE", "Create sales", "SALE", "CREATE"),
            createPermission("SALE_READ", "View sales", "SALE", "READ"),
            createPermission("SALE_UPDATE", "Update sales", "SALE", "UPDATE"),
            createPermission("SALE_DELETE", "Delete sales", "SALE", "DELETE"),

            // Purchase management
            createPermission("PURCHASE_CREATE", "Create purchases", "PURCHASE", "CREATE"),
            createPermission("PURCHASE_READ", "View purchases", "PURCHASE", "READ"),
            createPermission("PURCHASE_UPDATE", "Update purchases", "PURCHASE", "UPDATE"),
            createPermission("PURCHASE_DELETE", "Delete purchases", "PURCHASE", "DELETE"),

            // Report management
            createPermission("REPORT_VIEW", "View reports", "REPORT", "READ")
        );
        
        // Save permissions and store in map
        for (Permission permission : permissionList) {
            Permission savedPermission = permissionRepository.findByName(permission.getName())
                .orElseGet(() -> permissionRepository.save(permission));
            permissions.put(permission.getName(), savedPermission);
        }
        
        return permissions;
    }

    private void createSystemRoles(Map<String, Permission> permissions) {
        // Platform Admin role with all permissions
        createSystemRole(RoleConstants.PLATFORM_ADMIN, "Platform Administrator", new HashSet<>(permissions.values()));

        // GovernmentAgency Manager role with specific permissions
        createSystemRole(RoleConstants.PHARMACY_MANAGER, "GovernmentAgency Manager",
            new HashSet<>(Arrays.asList(
                permissions.get("EMPLOYEE_CREATE"),
                permissions.get("EMPLOYEE_READ"),
                permissions.get("EMPLOYEE_UPDATE"),
                permissions.get("EMPLOYEE_DELETE"),
                permissions.get("PHARMACY_UPDATE"),
                permissions.get("PHARMACY_READ"),
                permissions.get("USER_READ")
            )));

        // GovernmentAgency Employee role (permissions to be defined later)
        createSystemRole(RoleConstants.PHARMACY_EMPLOYEE, "GovernmentAgency Employee", new HashSet<>());

        // GovernmentAgency Trainer role (permissions to be defined later)
        createSystemRole(RoleConstants.PHARMACY_TRAINEE, "GovernmentAgency Trainer", new HashSet<>());
    }

    private Permission createPermission(String name, String description, String resource, String action) {
        return Permission.builder()
            .name(name)
            .description(description)
            .resource(resource)
            .action(action)
            .isActive(true)
            .isSystemGenerated(true)
            .build();
    }

    private void createSystemRole(String name, String description, Set<Permission> permissions) {
        Role role = roleRepository.findByName(name)
            .orElseGet(() -> {
                Role newRole = Role.builder()
                    .name(name)
                    .description(description)
                    .isActive(true)
                    .isSystem(true)
                    .isSystemGenerated(true)
                    .build();
                return roleRepository.save(newRole);
            });
        
        role.setPermissions(permissions);
        roleRepository.save(role);
    }
} 