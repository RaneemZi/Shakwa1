package com.Shakwa.user.dto;

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
@Schema(description = "Citizen Request", example = """
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "password": "Password123!",
}
""")
public class CitizenDTORequest {
 
    @Schema(description = "Name of the citizen", example = "John Doe")
    @NotBlank(message = "citizen name is required")
    private String name;

    @Schema(description = "Email address (required for registration)", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Password (required for registration)", example = "Password123!")
    private String password;



    // @Builder.Default
    // private boolean isActive = true;
}
