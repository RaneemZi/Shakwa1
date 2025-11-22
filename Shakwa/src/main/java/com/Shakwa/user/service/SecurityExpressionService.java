package com.Shakwa.user.service;

import com.Shakwa.user.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import com.Shakwa.user.entity.User;
import com.Shakwa.user.repository.CitizenRepo;
import com.Shakwa.user.repository.UserRepository;

@Service("authz")
public class SecurityExpressionService extends BaseSecurityService {


    public SecurityExpressionService(UserRepository userRepository, CitizenRepo citizenRepo, EmployeeRepository employeeRepository) {
        super(userRepository, citizenRepo , employeeRepository);
    }

    /**
     * Checks if the current user is a GovernmentAgency Manager
     */
    public boolean isGovernmentAgencyManager() {
        User currentUser = getCurrentUser();
        return currentUser.getRole().getName().equals("PHARMACY_MANAGER");
    }

    /**
     * Checks if the current user is a Pharmacist
     */
    public boolean isPharmacist() {
        User currentUser = getCurrentUser();
        return currentUser.getRole().getName().equals("PHARMACIST");
    }

    /**
     * Checks if the current user is a Trainee
     */
    public boolean isTrainee() {
        User currentUser = getCurrentUser();
        return currentUser.getRole().getName().equals("TRAINEE");
    }

    /**
     * Checks if the current user has a specific permission
     * @param permissionName The permission to check for
     * @return true if the user has the permission
     */
    public boolean hasPermission(String permissionName) {
        User currentUser = getCurrentUser();
        // Check role permissions
        boolean hasRolePermission = currentUser.getRole().getPermissions().stream()
                .anyMatch(permission -> permission.getName().equals(permissionName));
        if (hasRolePermission) {
            return true;
        }
        // Check additional permissions
        return currentUser.getAdditionalPermissions().stream()
                .anyMatch(permission -> permission.getName().equals(permissionName));
    }
} 