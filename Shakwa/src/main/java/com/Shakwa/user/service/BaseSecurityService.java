package com.Shakwa.user.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.Shakwa.user.Enum.GovernmentAgencyType;
import com.Shakwa.user.entity.Citizen;
import com.Shakwa.user.entity.Employee;
import com.Shakwa.user.entity.User;
import com.Shakwa.user.repository.CitizenRepo;
import com.Shakwa.user.repository.UserRepository;
import com.Shakwa.utils.exception.ResourceNotFoundException;
import com.Shakwa.utils.exception.UnAuthorizedException;

@Service
public abstract class BaseSecurityService {

    protected final UserRepository userRepository;
    protected final CitizenRepo citizenRepo;

    protected BaseSecurityService(UserRepository userRepository, CitizenRepo citizenRepo) {
        this.userRepository = userRepository;
        this.citizenRepo = citizenRepo;

    }

    /**
     * 
     * Gets the currently authenticated user
     * @return The current user
     * @throws ResourceNotFoundException if the user is not found
     */
    protected User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("User not authenticated");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    /**
     * Checks if the current user is a platform admin
     * @return true if the user is a platform admin
     */
    protected boolean isAdmin() {
        User currentUser = getCurrentUser();
        return currentUser.getRole().getName().equals("PLATFORM_ADMIN");
    }

    /**
     * Checks if the current user has a specific role
     * @param roleName The name of the role to check
     * @return true if the user has the specified role
     */
    protected boolean hasRole(String roleName) {
        User currentUser = getCurrentUser();
        return currentUser.getRole().getName().equals(roleName);
    }

    /**
     * Gets the governmentAgency associated with the currently authenticated user
     * @return GovernmentAgency object
     * @throws UnAuthorizedException if user is not an employee or has no governmentAgency
     */
    protected GovernmentAgencyType getCurrentUserGovernmentAgency() {
        User currentUser = getCurrentUser();
        if (currentUser instanceof Employee employee) {
            if (employee.getGovernmentAgency() == null) {
                throw new UnAuthorizedException("User is not associated with any governmentAgency");
            }
            return employee.getGovernmentAgency();
        }
        throw new UnAuthorizedException("User is not an employee");
    }

    /**
     * Gets the governmentAgency name of the currently authenticated user
     * @return GovernmentAgency name (enum name)
     * @throws UnAuthorizedException if user is not an employee or has no governmentAgency
     */
    protected String getCurrentUserGovernmentAgencyName() {
        return getCurrentUserGovernmentAgency().name();
    }

    /**
     * Validates that the current user has access to the specified governmentAgency
     * @param governmentAgency The governmentAgency to validate access for
     * @throws UnAuthorizedException if user doesn't have access
     */
    protected void validateGovernmentAgencyAccess(GovernmentAgencyType governmentAgency) {
        if (governmentAgency == null) {
            throw new UnAuthorizedException("GovernmentAgency is null");
        }
        GovernmentAgencyType currentUserGovernmentAgency = getCurrentUserGovernmentAgency();
        if (!currentUserGovernmentAgency.equals(governmentAgency)) {
            throw new UnAuthorizedException("User does not have access to governmentAgency: " + governmentAgency.getLabel());
        }
    }

    /**
     * Checks if the current user is an employee
     * @return true if user is an employee
     */
    protected boolean isCurrentUserEmployee() {
        try {
            User currentUser = getCurrentUser();
            return currentUser instanceof Employee;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets the currently authenticated citizen from the token
     * @return The current citizen
     * @throws UnAuthorizedException if the user is not a citizen or not authenticated
     */
    protected Citizen getCurrentCitizen() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnAuthorizedException("User not authenticated");
        }
        
        String email = authentication.getName();
        return citizenRepo.findByEmail(email)
                .orElseThrow(() -> new UnAuthorizedException("Current user is not a citizen"));
    }

    /**
     * Checks if the current authenticated user is a citizen
     * @return true if the current user is a citizen
     */
    protected boolean isCurrentUserCitizen() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return false;
            }
            String email = authentication.getName();
            return citizenRepo.findByEmail(email).isPresent();
        } catch (Exception e) {
            return false;
        }
    }
} 