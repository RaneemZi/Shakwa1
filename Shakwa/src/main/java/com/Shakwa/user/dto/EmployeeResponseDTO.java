package com.Shakwa.user.dto;

import java.time.LocalDate;

import com.Shakwa.user.Enum.UserStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;


@Data
public class EmployeeResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfHire;
    private String roleName;
    private UserStatus status;
    private Long governmentAgencyId;
} 