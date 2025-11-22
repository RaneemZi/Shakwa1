package com.Shakwa.complaint.mapper;

import org.springframework.stereotype.Component;

import com.Shakwa.complaint.dto.ComplaintHistoryDTO;
import com.Shakwa.complaint.entity.ComplaintHistory;

/**
 * Mapper لتحويل ComplaintHistory entity إلى DTO
 */
@Component
public class ComplaintHistoryMapper {
    
    public ComplaintHistoryDTO toDTO(ComplaintHistory history) {
        if (history == null) return null;
        
        ComplaintHistoryDTO dto = ComplaintHistoryDTO.builder()
                .id(history.getId())
                .actionType(history.getActionType())
                .fieldChanged(history.getFieldChanged())
                .oldValue(history.getOldValue())
                .newValue(history.getNewValue())
                .metadata(history.getMetadata())
                .actionDescription(history.getActionDescription())
                .createdAt(history.getCreatedAt())
                .build();
        
        // معلومات المستخدم الذي قام بالإجراء
        if (history.getActor() != null) {
            dto.setActorId(history.getActor().getId());
            dto.setActorName(history.getActor().getFirstName() + " " + history.getActor().getLastName());
            dto.setActorEmail(history.getActor().getEmail());
        }
        
        return dto;
    }
}

