package com.Shakwa.user.dto;

import com.Shakwa.user.Enum.GovernmentAgencyType;

import lombok.Data;

@Data
public class GovernmentAgencyResponseDTO {
    private Long id;
    private String governmentAgencyName;
    private String licenseNumber;
    private String address;
    private String email;
    private GovernmentAgencyType type;
    private String openingHours;
    private String phoneNumber;
    private String managerEmail;
    private String managerFirstName;
    private String managerLastName;
    private String areaName;
    private Long areaId;
    private String areaArabicName;
    
    // Account activation status
    private Boolean isActive;
} 