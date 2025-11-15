package com.Shakwa.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthenticationResponse {
    private String token;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    
    // Account activation status
    private Boolean isActive;
} 