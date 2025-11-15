package com.Shakwa.user.dto;

import java.time.LocalDateTime;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private RoleResponseDTO role;
    private Set<PermissionResponseDTO> additionalPermissions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    
    // GovernmentAgency information for employees
    private String governmentAgencyName;
    
    // Account activation status
    private Boolean isAccountActive;
} 