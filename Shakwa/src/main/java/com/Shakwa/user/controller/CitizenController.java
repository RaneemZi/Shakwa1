package com.Shakwa.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Shakwa.user.dto.AuthenticationRequest;
import com.Shakwa.user.dto.CitizenDTORequest;
import com.Shakwa.user.dto.CitizenDTOResponse;
import com.Shakwa.user.dto.OtpVerificationRequest;
import com.Shakwa.user.dto.PaginationDTO;
import com.Shakwa.user.dto.ResendOtpRequest;
import com.Shakwa.user.dto.UserAuthenticationResponse;
import com.Shakwa.user.service.CitizenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/v1/citizens")
@Tag(name = "Citizen Management", description = "APIs for managing citizens and their debts")
@CrossOrigin("*")
public class CitizenController {

    private final CitizenService citizenService;

    public CitizenController(CitizenService citizenService) {
        this.citizenService = citizenService;
    }

    @PostMapping("/register")
    @Operation(
        summary = "Register new citizen",
        description = "Register a new citizen with email, password, and name"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Registration successful",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = CitizenDTOResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "409", description = "Email already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CitizenDTOResponse> register(
            @Valid @RequestBody CitizenDTORequest dto) {
        CitizenDTOResponse citizen = citizenService.register(dto);
        return ResponseEntity.ok(citizen);
    }

    @PostMapping("/verify-otp")
    @Operation(
        summary = "Verify OTP",
        description = "Verify the OTP code sent to citizen's email to activate the account"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OTP verified successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid OTP or expired"),
        @ApiResponse(responseCode = "404", description = "OTP not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> verifyOtp(
            @Valid @RequestBody OtpVerificationRequest request) {
        citizenService.verifyOtp(request.getEmail(), request.getOtpCode());
        return ResponseEntity.ok("Email verified successfully. You can now login.");
    }

    @PostMapping("/resend-otp")
    @Operation(
        summary = "Resend OTP",
        description = "Resend OTP code to citizen's email"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OTP resent successfully"),
        @ApiResponse(responseCode = "404", description = "Citizen not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> resendOtp(
            @Valid @RequestBody ResendOtpRequest request) {
        citizenService.resendOtp(request.getEmail());
        return ResponseEntity.ok("OTP code has been sent to your email.");
    }

    @PostMapping("/login")
    @Operation(
        summary = "Citizen login",
        description = "Authenticates a citizen and returns a JWT token. Email must be verified first."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = UserAuthenticationResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials or email not verified"),
        @ApiResponse(responseCode = "429", description = "Too many login attempts"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<UserAuthenticationResponse> login(
            @Valid @RequestBody AuthenticationRequest request, 
            HttpServletRequest httpServletRequest) {
        UserAuthenticationResponse response = citizenService.login(request, httpServletRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all citizens", description = "Retrieve all citizens with their debt information with pagination")
    public ResponseEntity<PaginationDTO<CitizenDTOResponse>> getAllCitizens(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        PaginationDTO<CitizenDTOResponse> citizens = citizenService.getAllCitizens(page, size);
        return ResponseEntity.ok(citizens);
    }

  
    @GetMapping("{id}")
    @Operation(summary = "Get citizen by ID", description = "Retrieve a specific citizen by ID with debt details")
    public ResponseEntity<CitizenDTOResponse> getCitizenById(
            @Parameter(description = "Citizen ID", example = "1") 
            @PathVariable Long id) {
        CitizenDTOResponse citizen = citizenService.getCitizenById(id);
        return ResponseEntity.ok(citizen);
    }

    @GetMapping("search")
    @Operation(summary = "Search citizens by name", description = "Search citizens by name (partial match) with pagination")
    public ResponseEntity<PaginationDTO<CitizenDTOResponse>> searchCitizensByName(
            @Parameter(description = "Citizen name to search for", example = "cash") 
            @RequestParam(required = false) String name,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        PaginationDTO<CitizenDTOResponse> citizens = citizenService.searchCitizensByName(name, page, size);
        return ResponseEntity.ok(citizens);
    }


    @PostMapping
    @Operation(
        summary = "Create citizen", 
        description = "Create a new citizen. If name is not provided, 'cash citizen' will be used as default."
    )
    public ResponseEntity<CitizenDTOResponse> createCitizen(
            @Parameter(description = "Citizen data", required = true)
            @RequestBody CitizenDTORequest dto) {
        CitizenDTOResponse citizen = citizenService.createCitizen(dto);
        return ResponseEntity.ok(citizen);
    }

    @PutMapping("{id}")
    @Operation(summary = "Update citizen", description = "Update an existing citizen's information")
    public ResponseEntity<CitizenDTOResponse> updateCitizen(
            @Parameter(description = "Citizen ID", example = "1") 
            @PathVariable Long id,
            @Parameter(description = "Updated citizen data", required = true)
                                              @RequestBody CitizenDTORequest dto) {
        CitizenDTOResponse citizen = citizenService.updateCitizen(id, dto);
        return ResponseEntity.ok(citizen);
    }

    @DeleteMapping("{id}")
    @Operation(summary = "Delete citizen", description = "Delete a citizen. Cannot delete if citizen has active debts.")
    public ResponseEntity<Void> deleteCitizen(
            @Parameter(description = "Citizen ID", example = "1") 
            @PathVariable Long id) {
        citizenService.deleteCitizen(id);
        return ResponseEntity.ok().build();
    }
} 