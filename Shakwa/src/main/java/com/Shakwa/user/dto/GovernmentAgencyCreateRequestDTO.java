package com.Shakwa.user.dto;

import com.Shakwa.utils.annotation.ValidPassword;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GovernmentAgencyCreateRequestDTO {
    @NotBlank(message = "GovernmentAgency name couldn't be blank")
    private String governmentAgencyName;
    @NotBlank(message = "License number couldn't be blank")
    private String sector;
    private String email;
    private String websiteUrl;
    
    @NotBlank(message = "Manager password couldn't be blank")
    @ValidPassword
    private String managerPassword;
}