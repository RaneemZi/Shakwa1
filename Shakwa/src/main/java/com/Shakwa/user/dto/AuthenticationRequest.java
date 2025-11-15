package com.Shakwa.user.dto;

import com.Shakwa.utils.annotation.ValidPassword;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationRequest {

    @NotBlank(message = "Email couldn't be blank")
    private String email;
    @ValidPassword
    private String password;
}
