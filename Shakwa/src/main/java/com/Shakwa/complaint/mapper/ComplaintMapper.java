package com.Shakwa.complaint.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.Shakwa.complaint.Enum.ComplaintStatus;
import com.Shakwa.complaint.dto.ComplaintAttachmentDTO;
import com.Shakwa.complaint.dto.ComplaintDTORequest;
import com.Shakwa.complaint.dto.ComplaintDTOResponse;
import com.Shakwa.complaint.entity.Complaint;
import com.Shakwa.complaint.entity.ComplaintAttachment;

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
                .attachments(mapAttachments(complaint))
                .createdAt(complaint.getCreatedAt())
                .updatedAt(complaint.getUpdatedAt())
                .trackingNumber(complaint.getTrackingNumber())
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

        // Version for optimistic locking
        response.setVersion(complaint.getVersion());

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
        complaint.setStatus(dto.getStatus() != null ? dto.getStatus() : ComplaintStatus.PENDING);
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
    }

    private List<ComplaintAttachmentDTO> mapAttachments(Complaint complaint) {
        if (complaint.getAttachments() == null || complaint.getAttachments().isEmpty()) {
            return Collections.emptyList();
        }
        return complaint.getAttachments().stream()
                .map(attachment -> ComplaintAttachmentDTO.builder()
                        .id(attachment.getId())
                        .originalFilename(attachment.getOriginalFilename())
                        .size(attachment.getSize())
                        .contentType(attachment.getContentType())
                        .uploadedAt(attachment.getUploadedAt())
                        .downloadUrl(buildDownloadUrl(complaint.getId(), attachment.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    private String buildDownloadUrl(Long complaintId, Long attachmentId) {
        if (complaintId == null || attachmentId == null) {
            return null;
        }
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/complaints/")
                .path(String.valueOf(complaintId))
                .path("/attachments/")
                .path(String.valueOf(attachmentId))
                .toUriString();
    }
}

