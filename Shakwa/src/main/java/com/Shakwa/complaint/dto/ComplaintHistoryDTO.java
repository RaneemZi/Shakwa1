package com.Shakwa.complaint.dto;

import java.time.LocalDateTime;

import com.Shakwa.complaint.Enum.HistoryActionType;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO لسجل تغييرات الشكوى
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintHistoryDTO {
    
    private Long id;
    
    private HistoryActionType actionType;
    
    private String fieldChanged;
    
    private String oldValue;
    
    private String newValue;
    
    private String metadata;
    
    /**
     * وصف الإجراء بالعربية
     */
    private String actionDescription;
    
    /**
     * معلومات المستخدم الذي قام بالإجراء
     */
    private Long actorId;
    private String actorName;
    private String actorEmail;
    
    /**
     * تاريخ إنشاء السجل
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}

