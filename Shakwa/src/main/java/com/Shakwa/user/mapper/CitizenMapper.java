package com.Shakwa.user.mapper;

import com.Shakwa.user.Enum.UserStatus;
import com.Shakwa.user.config.RoleConstants;
import com.Shakwa.user.repository.RoleRepository;
import org.springframework.stereotype.Component;

import com.Shakwa.user.dto.CitizenDTORequest;
import com.Shakwa.user.dto.CitizenDTOResponse;
import com.Shakwa.user.entity.Citizen;

import static com.Shakwa.user.config.RoleConstants.CITIZEN;


@Component
public class CitizenMapper {


    private final RoleRepository roleRepository;

    public CitizenMapper(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public CitizenDTOResponse toResponse(Citizen citizen) {
        if (citizen == null) return null;
        
        CitizenDTOResponse response = CitizenDTOResponse.builder()
                .id(citizen.getId())
                .name(citizen.getFirstName() + " " + citizen.getLastName())
                .email(citizen.getEmail())
                .build();

        return response;
    }


    public Citizen toEntity(CitizenDTORequest dto) {
        if (dto == null) return null;
       
        Citizen citizen = new Citizen();
        citizen.setFirstName(dto.getName().split(" ")[0]);
        citizen.setLastName(dto.getName().split(" ")[1]);
        citizen.setEmail(dto.getEmail());
        citizen.setPassword(dto.getPassword());
        citizen.setRole(roleRepository.findByName(CITIZEN).orElse(null));
        citizen.setStatus(UserStatus.INACTIVE);
        return citizen;
    }

    public void updateEntityFromDto(Citizen citizen, CitizenDTORequest dto) {
        if (dto == null || citizen == null) return;
        
        citizen.setFirstName(dto.getName().split(" ")[0]);
        citizen.setLastName(dto.getName().split(" ")[1]);
    }
}
