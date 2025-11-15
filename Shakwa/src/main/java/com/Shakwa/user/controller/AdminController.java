package com.Shakwa.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Shakwa.user.dto.AuthenticationRequest;
import com.Shakwa.user.dto.UserAuthenticationResponse;
import com.Shakwa.user.service.UserService;
import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Management", description = "APIs for platform administration and governmentAgency creation")
@SecurityRequirement(name = "BearerAuth")
@CrossOrigin("*")
public class AdminController {
   
    private final UserService userService;

    @PostMapping("/login")
    @Operation(
        summary = "Admin login",
        description = "Authenticates a platform admin and returns a JWT token"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = UserAuthenticationResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "429", description = "Too many login attempts"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<UserAuthenticationResponse> adminLogin(
            @Valid @RequestBody AuthenticationRequest request, 
            HttpServletRequest httpServletRequest) {
        UserAuthenticationResponse response = userService.login(request, httpServletRequest);
        return ResponseEntity.ok(response);
    }


} 