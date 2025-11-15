package com.Shakwa.user.mapper;

import com.Shakwa.user.dto.EmployeeCreateRequestDTO;
import com.Shakwa.user.dto.EmployeeResponseDTO;
import com.Shakwa.user.dto.EmployeeUpdateRequestDTO;
import com.Shakwa.user.entity.Employee;


public class EmployeeMapper {
    
    // Map from CreateRequestDTO to new Employee entity
    public static Employee toEntity(EmployeeCreateRequestDTO dto) {
        if (dto == null) return null;
        Employee employee = new Employee();
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setPassword(dto.getPassword());
        employee.setPhoneNumber(dto.getPhoneNumber());
        employee.setDateOfHire(dto.getDateOfHire());
        employee.setStatus(dto.getStatus());
        // Don't set working hours here - will be handled in service after employee is saved
        // email and governmentAgency will be set in service
        return employee;
    }

    // Update existing Employee entity from UpdateRequestDTO
    public static void updateEntity(Employee employee, EmployeeUpdateRequestDTO dto) {
        if (dto == null || employee == null) return;
        
        if (dto.getFirstName() != null) {
            employee.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null) {
            employee.setLastName(dto.getLastName());
        }
        if (dto.getPhoneNumber() != null) {
            employee.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getDateOfHire() != null) {
            employee.setDateOfHire(dto.getDateOfHire());
        }

        if (dto.getStatus() != null) {
            employee.setStatus(dto.getStatus());
        }
    }

    // Map from Employee entity to ResponseDTO
    public static EmployeeResponseDTO toResponseDTO(Employee entity) {
        if (entity == null) return null;
        EmployeeResponseDTO dto = new EmployeeResponseDTO();
        dto.setId(entity.getId());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setEmail(entity.getEmail());
        dto.setPhoneNumber(entity.getPhoneNumber());
        dto.setDateOfHire(entity.getDateOfHire());
        dto.setRoleName(entity.getRole() != null ? entity.getRole().getName() : null);
        dto.setStatus(entity.getStatus());
        dto.setGovernmentAgencyId(entity.getGovernmentAgency() != null ? (long) entity.getGovernmentAgency().ordinal() : null);
        
                     
        return dto;
    }
    
   
} 