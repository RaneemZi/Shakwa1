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
import com.Shakwa.user.entity.User;
import com.Shakwa.user.repository.PermissionRepository;
import com.Shakwa.user.repository.RoleRepository;
import com.Shakwa.user.repository.UserRepository;
import com.Shakwa.user.config.RoleConstants;

import lombok.RequiredArgsConstructor;
import jakarta.persistence.EntityManager;
@Component
@RequiredArgsConstructor
@Order(1) // Run after database migration
public class SystemRolesInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(SystemRolesInitializer.class);

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Initializing system roles and permissions...");

        // Always create/update permissions
        Map<String, Permission> permissions = createPermissions();

        // Always create/update system roles (will update if exist, create if not)
        createSystemRoles(permissions);
        
//        // Remove old roles that are no longer needed
//        removeOldRoles();
        
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

            // Citizen management
            createPermission("CITIZEN_CREATE", "Create citizens", "CITIZEN", "CREATE"),
            createPermission("CITIZEN_READ", "View citizens", "CITIZEN", "READ"),
            createPermission("CITIZEN_UPDATE", "Update citizens", "CITIZEN", "UPDATE"),
            createPermission("CITIZEN_DELETE", "Delete citizens", "CITIZEN", "DELETE"),

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

        // Supervisor role with management permissions
        createSystemRole(RoleConstants.SUPERVISOR, "Supervisor",
            new HashSet<>(Arrays.asList(
                permissions.get("EMPLOYEE_CREATE"),
                permissions.get("EMPLOYEE_READ"),
                permissions.get("EMPLOYEE_UPDATE"),
                permissions.get("EMPLOYEE_DELETE"),
                permissions.get("USER_READ"),
                permissions.get("USER_UPDATE"),
                permissions.get("CITIZEN_READ"),
                permissions.get("CITIZEN_UPDATE"),
                permissions.get("REPORT_VIEW")
            )));

        // Viewer role with read-only permissions
        createSystemRole(RoleConstants.VIEWER, "Viewer",
            new HashSet<>(Arrays.asList(
                permissions.get("EMPLOYEE_READ"),
                permissions.get("PHARMACY_READ"),
                permissions.get("USER_READ"),
                permissions.get("CITIZEN_READ"),
                permissions.get("REPORT_VIEW")
            )));

        // Citizen role
        createSystemRole(RoleConstants.CITIZEN, "Citizen",
                new HashSet<>(Arrays.asList()));
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
                log.info("Creating new role: {}", name);
                Role newRole = Role.builder()
                    .name(name)
                    .description(description)
                    .isActive(true)
                    .isSystem(true)
                    .isSystemGenerated(true)
                    .build();
                return roleRepository.save(newRole);
            });
        
        // Update role permissions and description even if role exists
        log.info("Updating role: {} with {} permissions", name, permissions.size());
        role.setDescription(description);
        role.setPermissions(permissions);
        role.setActive(true);
        roleRepository.save(role);
    }
    
//    /**
//     * Remove old roles that are no longer used in the system
//     */
//    private void removeOldRoles() {
//        List<String> oldRoleNames = Arrays.asList(
//            "PHARMACY_MANAGER",
//            "PHARMACY_EMPLOYEE",
//            "PHARMACY_TRAINEE"
//        );
//
//        for (String oldRoleName : oldRoleNames) {
//            roleRepository.findByName(oldRoleName).ifPresent(role -> {
//                try {
//                    // Check if any users are using this role
//                    List<User> usersWithRole = userRepository.findAll().stream()
//                            .filter(user -> user.getRole() != null && user.getRole().getId().equals(role.getId()))
//                            .toList();
//
//                    if (!usersWithRole.isEmpty()) {
//                        log.warn("Cannot delete role '{}' because {} user(s) are using it. Please migrate users to SUPERVISOR or VIEWER first.",
//                                oldRoleName, usersWithRole.size());
//                        log.warn("Users using this role: {}",
//                                usersWithRole.stream()
//                                        .map(u -> u.getEmail() + " (ID: " + u.getId() + ")")
//                                        .collect(java.util.stream.Collectors.joining(", ")));
//                        // Deactivate instead of delete if users exist
//                        role.setActive(false);
//                        roleRepository.save(role);
//                    } else {
//                        // Safe to delete - no users are using this role
//                        log.info("Deleting old role '{}' (no users are using it)", oldRoleName);
//                        roleRepository.delete(role);
//                        log.info("Successfully deleted role '{}'", oldRoleName);
//                    }
//                } catch (Exception e) {
//                    log.error("Error while trying to delete role '{}': {}", oldRoleName, e.getMessage());
//                    // Fallback: deactivate instead of delete
//                    role.setActive(false);
//                    roleRepository.save(role);
//                    log.warn("Deactivated role '{}' instead of deleting due to error", oldRoleName);
//                }
//            });
//        }
//    }
}