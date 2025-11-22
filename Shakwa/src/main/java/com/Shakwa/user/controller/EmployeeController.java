package com.Shakwa.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.Shakwa.user.dto.AuthenticationRequest;
import com.Shakwa.user.dto.EmployeeCreateRequestDTO;
import com.Shakwa.user.dto.EmployeeResponseDTO;
import com.Shakwa.user.dto.EmployeeUpdateRequestDTO;
import com.Shakwa.user.dto.UserAuthenticationResponse;
import com.Shakwa.user.service.EmployeeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@Tag(name = "Employee Management", description = "APIs for managing governmentAgency employees and their working hours")
@SecurityRequirement(name = "BearerAuth")
@CrossOrigin("*")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
//    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Add new employee",
        description = "Creates a new employee in a governmentAgency. Requires PLATFORM_ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully created employee",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = EmployeeResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid employee data"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<EmployeeResponseDTO> addEmployee(
            @Parameter(description = "Employee data", required = true)
            @Valid @RequestBody EmployeeCreateRequestDTO dto) {
        return ResponseEntity.ok(employeeService.addEmployee(dto));
    }

    @GetMapping
//    @PreAuthorize("hasRole('PLATFORM_ADMIN ')")
    @Operation(
        summary = "Get all employees in governmentAgency",
        description = "Retrieves all employees in the current governmentAgency. Requires PLATFORM_ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all employees",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = EmployeeResponseDTO.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<EmployeeResponseDTO>> getAllEmployeesInGovernmentAgency() {
        return ResponseEntity.ok(employeeService.getAllEmployeesInGovernmentAgency());
    }

    @GetMapping("/{employeeId}")
//    @PreAuthorize("hasRole('PHARMACY_MANAGER')")
    @Operation(
        summary = "Get employee by ID",
        description = "Retrieves a specific employee by ID. Requires PHARMACY_MANAGER role and employee must belong to the same governmentAgency."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved employee",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = EmployeeResponseDTO.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions or employee not in same governmentAgency"),
        @ApiResponse(responseCode = "404", description = "Employee not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(
            @Parameter(description = "Employee ID", example = "1") @PathVariable Long employeeId) {
        return ResponseEntity.ok(employeeService.getEmployeeByIdWithAuth(employeeId));
    }

    @PutMapping("/{employeeId}")
//    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Update employee",
        description = "Updates an existing employee's information. Password cannot be updated for security reasons. Requires PLATFORM_ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated employee",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = EmployeeResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid employee data"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Employee not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<EmployeeResponseDTO> updateEmployeeInGovernmentAgency(
            @Parameter(description = "Employee ID", example = "1") @PathVariable Long employeeId, 
            @Parameter(description = "Updated employee data (password cannot be updated)", required = true)
            @Valid @RequestBody EmployeeUpdateRequestDTO dto) {
        return ResponseEntity.ok(employeeService.updateEmployeeInGovernmentAgency(employeeId, dto));
    }

    @DeleteMapping("/{employeeId}")
//    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Delete employee",
        description = "Deletes an employee from the governmentAgency. Requires PLATFORM_ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully deleted employee"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Employee not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteEmployeeInGovernmentAgency(
            @Parameter(description = "Employee ID", example = "1") @PathVariable Long employeeId) {
        employeeService.deleteEmployeeInGovernmentAgency(employeeId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/login")
    @Operation(
        summary = "Employee login",
        description = "Authenticates an employee using email and password set by platform admin. Returns a JWT token."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = UserAuthenticationResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials or account not active"),
        @ApiResponse(responseCode = "429", description = "Too many login attempts"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<UserAuthenticationResponse> login(
            @Parameter(description = "Login credentials", required = true)
            @Valid @RequestBody AuthenticationRequest request,
            HttpServletRequest httpServletRequest) {
        UserAuthenticationResponse response = employeeService.login(request, httpServletRequest);
        return ResponseEntity.ok(response);
    }
 
} 