package com.Shakwa.user.mapper;

import org.springframework.stereotype.Component;

import com.Shakwa.user.dto.PermissionResponseDTO;
import com.Shakwa.user.entity.Permission;

@Component
public class PermissionMapper {

    public PermissionResponseDTO toResponse(Permission permission) {
        if (permission == null) {
            return null;
        }

        return PermissionResponseDTO.builder()
                .id(permission.getId())
                .name(permission.getName())
                .description(permission.getDescription())
                .resource(permission.getResource())
                .action(permission.getAction())
                .isActive(permission.isActive())
                .isSystemGenerated(permission.isSystemGenerated())
                .build();
    }
} 