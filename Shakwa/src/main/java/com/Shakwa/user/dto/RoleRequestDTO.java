package com.Shakwa.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequestDTO {
    
    @NotBlank(message = "Role name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Active status is required")
    private Boolean isActive;
    
    private Set<Long> permissionIds;
} 