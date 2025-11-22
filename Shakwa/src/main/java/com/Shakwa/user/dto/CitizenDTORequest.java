package com.Shakwa.user.dto;

import com.Shakwa.utils.annotation.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Citizen Request")
public class CitizenDTORequest {
 
    @Schema(description = "Name of the citizen", example = "John Doe")
    @NotBlank(message = "citizen name is required")
    private String name;

    @Schema(description = "Email address (required for registration)", example = "john.doe@example.com")
    @NotBlank
    private String email;

    @Schema(description = "Password (required for registration)", example = "Password123!")
    @ValidPassword
    private String password;

    // @Builder.Default
    // private boolean isActive = true;
}
