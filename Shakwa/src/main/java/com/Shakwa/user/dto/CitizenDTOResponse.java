package com.Shakwa.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Citizen response with debt information")
public class CitizenDTOResponse {
    private Long id;
    
    @Schema(description = "Citizen name", example = "cash citizen")
    private String name;

    @Schema(description = "Citizen email", example = "cashcitizen@gmail.com")
    private String email;


}
