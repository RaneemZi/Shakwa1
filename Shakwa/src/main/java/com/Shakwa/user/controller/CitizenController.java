package com.Shakwa.user.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(CitizenController.class);

    public CitizenController(CitizenService citizenService) {
        this.citizenService = citizenService;
        logger.info("CitizenController initialized successfully");
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
        logger.info("Registering new citizen with email: {}", dto.getEmail());
        CitizenDTOResponse citizen = citizenService.register(dto);
        logger.info("Successfully registered citizen with ID: {}", citizen.getId());
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
        logger.info("Verifying OTP for email: {}", request.getEmail());
        citizenService.verifyOtp(request.getEmail(), request.getOtpCode());
        logger.info("OTP verified successfully for email: {}", request.getEmail());
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
        logger.info("Resending OTP for email: {}", request.getEmail());
        citizenService.resendOtp(request.getEmail());
        logger.info("OTP resent successfully for email: {}", request.getEmail());
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
        logger.info("Citizen login attempt for email: {}", request.getEmail());
        UserAuthenticationResponse response = citizenService.login(request, httpServletRequest);
        logger.info("Citizen login successful for email: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all citizens", description = "Retrieve all citizens with their debt information")
    public ResponseEntity<List<CitizenDTOResponse>> getAllCitizens() {
        logger.info("Fetching all citizens");
        List<CitizenDTOResponse> citizens = citizenService.getAllCitizens();
        logger.info("Retrieved {} citizens", citizens.size());
        return ResponseEntity.ok(citizens);
    }

  
    @GetMapping("{id}")
    @Operation(summary = "Get citizen by ID", description = "Retrieve a specific citizen by ID with debt details")
    public ResponseEntity<CitizenDTOResponse> getCitizenById(
            @Parameter(description = "Citizen ID", example = "1") 
            @PathVariable Long id) {
        logger.info("Fetching citizen with ID: {}", id);
        CitizenDTOResponse citizen = citizenService.getCitizenById(id);
        logger.info("Retrieved citizen: {}", citizen.getName());
        return ResponseEntity.ok(citizen);
    }

      @GetMapping("search")
    @Operation(summary = "Search citizens by name", description = "Search citizens by name (partial match)")
    public ResponseEntity<List<CitizenDTOResponse>> searchCitizensByName(
            @Parameter(description = "Citizen name to search for", example = "cash") 
            @RequestParam(required = false) String name) {
        logger.info("Searching citizens with name: {}", name);
        List<CitizenDTOResponse> citizens = citizenService.searchCitizensByName(name);
        logger.info("Found {} citizens matching search criteria", citizens.size());
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
        logger.info("Creating new citizen with name: {}", dto.getName());
        CitizenDTOResponse citizen = citizenService.createCitizen(dto);
        logger.info("Successfully created citizen with ID: {}", citizen.getId());
        return ResponseEntity.ok(citizen);
    }

    @PutMapping("{id}")
    @Operation(summary = "Update citizen", description = "Update an existing citizen's information")
    public ResponseEntity<CitizenDTOResponse> updateCitizen(
            @Parameter(description = "Citizen ID", example = "1") 
            @PathVariable Long id,
            @Parameter(description = "Updated citizen data", required = true)
                                              @RequestBody CitizenDTORequest dto) {
        logger.info("Updating citizen with ID: {}", id);
        CitizenDTOResponse citizen = citizenService.updateCitizen(id, dto);
        logger.info("Successfully updated citizen: {}", citizen.getName());
        return ResponseEntity.ok(citizen);
    }

    @DeleteMapping("{id}")
    @Operation(summary = "Delete citizen", description = "Delete a citizen. Cannot delete if citizen has active debts.")
    public ResponseEntity<Void> deleteCitizen(
            @Parameter(description = "Citizen ID", example = "1") 
            @PathVariable Long id) {
        logger.info("Deleting citizen with ID: {}", id);
        citizenService.deleteCitizen(id);
        logger.info("Successfully deleted citizen with ID: {}", id);
        return ResponseEntity.ok().build();
    }
} 