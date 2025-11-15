package com.Shakwa.user.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.Shakwa.user.dto.RoleRequestDTO;
import com.Shakwa.user.dto.RoleResponseDTO;
import com.Shakwa.user.entity.Role;
import com.Shakwa.user.service.RoleService;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "Role Management", description = "APIs for managing system roles and permissions")
@CrossOrigin("*")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Get all roles",
        description = "Retrieves a list of all roles in the system. Requires PLATFORM_ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all roles",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = RoleResponseDTO.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        return new ResponseEntity<>(roles, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Get role by ID",
        description = "Retrieves a specific role by its ID. Requires PLATFORM_ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the role",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = RoleResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Role not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getRoleById(
            @Parameter(description = "ID of the role", required = true) @PathVariable("id") Long id) {
        Role role = roleService.getRoleById(id);
        return new ResponseEntity<>(role, HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Create new role",
        description = "Creates a new role in the system. Requires PLATFORM_ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Successfully created the role",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = RoleResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> createRole(
            @Parameter(description = "Role data", required = true) @RequestBody RoleRequestDTO role) {
        Role createdRole = roleService.createRole(role);
        return new ResponseEntity<>(createdRole, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Update role",
        description = "Updates an existing role. Requires PLATFORM_ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated the role",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = RoleResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Role not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> updateRole(
            @Parameter(description = "ID of the role", required = true) @PathVariable("id") Long id,
            @Parameter(description = "Updated role data", required = true) @RequestBody RoleRequestDTO roleDetails) {
        Role updatedRole = roleService.updateRole(id, roleDetails);
        return new ResponseEntity<>(updatedRole, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Delete role",
        description = "Deletes a role from the system. Requires PLATFORM_ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully deleted the role"),
        @ApiResponse(responseCode = "404", description = "Role not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteRole(
            @Parameter(description = "ID of the role", required = true) @PathVariable("id") Long id) {
        roleService.deleteRole(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Get role permissions",
        description = "Retrieves all permissions associated with a specific role. Requires PLATFORM_ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved role permissions",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "404", description = "Role not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<String>> getRolePermissions(
            @Parameter(description = "ID of the role", required = true) @PathVariable("id") Long id) {
        List<String> permissions = roleService.getPermissionsByRoleId(id);
        return new ResponseEntity<>(permissions, HttpStatus.OK);
    }

    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Update role permissions",
        description = "Updates the permissions associated with a specific role. Requires PLATFORM_ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated role permissions"),
        @ApiResponse(responseCode = "404", description = "Role not found"),
        @ApiResponse(responseCode = "400", description = "Invalid permission data"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> updateRolePermissions(
            @Parameter(description = "ID of the role", required = true) @PathVariable("id") Long id,
            @Parameter(description = "List of permissions", required = true) @RequestBody Set<Long> permissions) {
        Role updatedRole = roleService.updateRolePermissions(id, permissions);
        return ResponseEntity.ok(updatedRole);
    }

    @GetMapping("/employee-roles")
    @Operation(
        summary = "Get employee roles",
        description = "Retrieves a list of roles that can be assigned to employees (excludes PLATFORM_ADMIN). This endpoint is public and can be accessed without authentication."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved employee roles",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = Role.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Role>> getEmployeeRoles() {
        List<Role> roles = roleService.getEmployeeRoles();
        return new ResponseEntity<>(roles, HttpStatus.OK);
    }
} 