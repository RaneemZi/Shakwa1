package com.Shakwa.user.mapper;

import org.springframework.stereotype.Component;

import com.Shakwa.user.dto.ComplaintDTORequest;
import com.Shakwa.user.dto.ComplaintDTOResponse;
import com.Shakwa.user.entity.Complaint;

@Component
public class ComplaintMapper {

    public ComplaintDTOResponse toResponse(Complaint complaint) {
        if (complaint == null) return null;

        ComplaintDTOResponse response = ComplaintDTOResponse.builder()
                .id(complaint.getId())
                .complaintType(complaint.getComplaintType())
                .governorate(complaint.getGovernorate())
                .governmentAgency(complaint.getGovernmentAgency())
                .location(complaint.getLocation())
                .description(complaint.getDescription())
                .solutionSuggestion(complaint.getSolutionSuggestion())
                .status(complaint.getStatus())
                .response(complaint.getResponse())
                .respondedAt(complaint.getRespondedAt())
                .attachments(complaint.getAttachments() != null ? complaint.getAttachments() : new java.util.ArrayList<>())
                .build();

        // معلومات المواطن
        if (complaint.getCitizen() != null) {
            response.setCitizenId(complaint.getCitizen().getId());
            response.setCitizenName(complaint.getCitizen().getFirstName() + " " + complaint.getCitizen().getLastName());
        }

        // معلومات الموظف الذي رد
        if (complaint.getRespondedBy() != null) {
            response.setRespondedById(complaint.getRespondedBy().getId());
            response.setRespondedByName(complaint.getRespondedBy().getFirstName() + " " + complaint.getRespondedBy().getLastName());
        }

        return response;
    }

    public Complaint toEntity(ComplaintDTORequest dto) {
        if (dto == null) return null;

        Complaint complaint = new Complaint();
        complaint.setComplaintType(dto.getComplaintType());
        complaint.setGovernorate(dto.getGovernorate());
        complaint.setGovernmentAgency(dto.getGovernmentAgency());
        complaint.setLocation(dto.getLocation());
        complaint.setDescription(dto.getDescription());
        complaint.setSolutionSuggestion(dto.getSolutionSuggestion());
        complaint.setStatus(dto.getStatus() != null ? dto.getStatus() : com.Shakwa.user.Enum.ComplaintStatus.PENDING);
        complaint.setAttachments(dto.getAttachments() != null ? dto.getAttachments() : new java.util.ArrayList<>());

        return complaint;
    }

    public void updateEntityFromDto(Complaint complaint, ComplaintDTORequest dto) {
        if (dto == null || complaint == null) return;

        if (dto.getComplaintType() != null) {
            complaint.setComplaintType(dto.getComplaintType());
        }
        if (dto.getGovernorate() != null) {
            complaint.setGovernorate(dto.getGovernorate());
        }
        if (dto.getGovernmentAgency() != null) {
            complaint.setGovernmentAgency(dto.getGovernmentAgency());
        }
        if (dto.getLocation() != null) {
            complaint.setLocation(dto.getLocation());
        }
        if (dto.getDescription() != null) {
            complaint.setDescription(dto.getDescription());
        }
        if (dto.getSolutionSuggestion() != null) {
            complaint.setSolutionSuggestion(dto.getSolutionSuggestion());
        }
        if (dto.getStatus() != null) {
            complaint.setStatus(dto.getStatus());
        }
        if (dto.getAttachments() != null) {
            complaint.setAttachments(dto.getAttachments());
        }
    }
}

