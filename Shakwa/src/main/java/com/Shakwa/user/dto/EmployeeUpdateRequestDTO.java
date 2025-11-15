package com.Shakwa.user.dto;

import com.Shakwa.user.Enum.UserStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDate;

@Data
@Schema(description = "Request to update an existing employee (password cannot be updated)")
public class EmployeeUpdateRequestDTO {
    @Schema(description = "Employee's first name", example = "John")
    private String firstName;
    
    @Schema(description = "Employee's last name", example = "Doe")
    private String lastName;
    
    @Schema(description = "Employee's phone number", example = "1234567890")
    private String phoneNumber;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Employee's hire date", example = "2024-01-15")
    private LocalDate dateOfHire;
    
    @Schema(description = "Role name for the employee (SUPERVISOR or VIEWER). Either roleId or roleName must be provided", example = "SUPERVISOR")
    private String roleName;

    private UserStatus status;
    

} 