package com.Shakwa.user.mapper;

import org.springframework.stereotype.Component;

import com.Shakwa.user.dto.RoleResponseDTO;
import com.Shakwa.user.entity.Role;

import java.util.stream.Collectors;

@Component
public class RoleMapper {

    private final PermissionMapper permissionMapper;

    public RoleMapper(PermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    public RoleResponseDTO toResponse(Role role) {
        if (role == null) {
            return null;
        }

        return RoleResponseDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .isActive(role.isActive())
                .isSystem(role.isSystem())
                .isSystemGenerated(role.isSystemGenerated())
                .permissions(role.getPermissions() != null ? 
                    role.getPermissions().stream()
                        .map(permissionMapper::toResponse)
                        .collect(Collectors.toSet()) : null)
                .build();
    }
} 