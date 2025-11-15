package com.Shakwa.user.mapper;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.Shakwa.user.Enum.GovernmentAgencyType;
import com.Shakwa.user.dto.UserResponseDTO;
import com.Shakwa.user.entity.Employee;
import com.Shakwa.user.entity.User;

@Component
public class UserMapper {

    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;

    public UserMapper(RoleMapper roleMapper, PermissionMapper permissionMapper) {
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
    }

    public UserResponseDTO toResponse(User user) {
        if (user == null) {
            return null;
        }

        UserResponseDTO response = new UserResponseDTO();
        
        // Set basic user fields
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        
        // Set role and permissions
        if (user.getRole() != null) {
            response.setRole(roleMapper.toResponse(user.getRole()));
        }
        
        if (user.getAdditionalPermissions() != null) {
            response.setAdditionalPermissions(user.getAdditionalPermissions().stream()
                    .map(permissionMapper::toResponse)
                    .collect(Collectors.toSet()));
        }

        // Add governmentAgency information if user is an employee
        if (user instanceof Employee employee) {
            GovernmentAgencyType governmentAgency = employee.getGovernmentAgency();
            if (governmentAgency != null) {
                response.setGovernmentAgencyName(governmentAgency.getLabel());
                boolean isAccountActive = isGovernmentAgencyAccountActive(governmentAgency);
                response.setIsAccountActive(isAccountActive);
            } else {
                response.setGovernmentAgencyName(null);
                response.setIsAccountActive(false);
            }
        } else {
            // For non-employee users (like platform admin), set default values
            response.setGovernmentAgencyName(null);
            response.setIsAccountActive(true); // Platform admin accounts are always active
        }

        return response;
    }
    
    /**
     * Determines if a governmentAgency account is active based on completion of registration data
     * @param governmentAgency The governmentAgency entity
     * @return true if the governmentAgency has complete registration data
     */
    private boolean isGovernmentAgencyAccountActive(GovernmentAgencyType governmentAgency) {
        if (governmentAgency == null) {
            return false;
        }
        
       return true;
    }
} 