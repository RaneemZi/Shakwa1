package com.Shakwa.user.dto;

import com.Shakwa.user.Enum.GovernmentAgencyType;
import com.Shakwa.user.Enum.UserStatus;
import com.Shakwa.utils.annotation.ValidPassword;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;


@Data
@Schema(description = "Request to create a new employee")
public class EmployeeCreateRequestDTO {
    @Schema(description = "Employee's first name", example = "John")
    private String firstName;
    
    @Schema(description = "Employee's last name", example = "Doe")
    private String lastName;
    
    @Schema(description = "Employee's password", example = "Password!1")
    @ValidPassword
    private String password;
    
    @Schema(description = "Employee's phone number", example = "1234567890")
    private String phoneNumber;
    
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Employee's hire date", example = "2024-01-15")
    private LocalDate dateOfHire;
    
    // @Schema(description = "Role ID for the employee (optional if roleName is provided)", example = "2")
    // private Long roleId;
    
    @Schema(description = "Role name for the employee (SUPERVISOR or VIEWER). Either roleId or roleName must be provided", example = "SUPERVISOR")
    private String roleName;

    private UserStatus status;
    
    @Schema(description = "Government agency type for the employee", example = "وزارة_الصحة", required = true)
    @NotNull(message = "Government agency type is required")
    private GovernmentAgencyType governmentAgencyType;
    
}