package com.Shakwa.complaint.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Shakwa.complaint.Enum.ComplaintStatus;
import com.Shakwa.complaint.Enum.HistoryActionType;
import com.Shakwa.complaint.dto.ComplaintHistoryDTO;
import com.Shakwa.complaint.entity.Complaint;
import com.Shakwa.complaint.entity.ComplaintHistory;
import com.Shakwa.complaint.repository.ComplaintHistoryRepository;
import com.Shakwa.user.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service لإدارة سجل تغييرات الشكوى
 */
@Service
@Transactional
public class ComplaintHistoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(ComplaintHistoryService.class);
    
    private final ComplaintHistoryRepository complaintHistoryRepository;
    private final ObjectMapper objectMapper;
    
    public ComplaintHistoryService(ComplaintHistoryRepository complaintHistoryRepository,
                                   ObjectMapper objectMapper) {
        this.complaintHistoryRepository = complaintHistoryRepository;
        this.objectMapper = objectMapper;
    }
    
    /**
     * تسجيل إنشاء شكوى جديدة
     */
    public void recordCreation(Complaint complaint, User actor) {
        ComplaintHistory history = new ComplaintHistory(complaint, actor, HistoryActionType.CREATED);
        history.setActionDescription(generateActionDescription(HistoryActionType.CREATED, actor, null, null, null));
        complaintHistoryRepository.save(history);
    }
    
    /**
     * تسجيل تغيير حالة الشكوى
     */
    public void recordStatusChange(Complaint complaint, User actor, ComplaintStatus oldStatus, ComplaintStatus newStatus) {
        ComplaintHistory history = new ComplaintHistory(complaint, actor, HistoryActionType.STATUS_CHANGED);
        history.setFieldChanged("status");
        history.setOldValue(oldStatus != null ? oldStatus.name() : null);
        history.setNewValue(newStatus != null ? newStatus.name() : null);
        history.setActionDescription(generateActionDescription(HistoryActionType.STATUS_CHANGED, actor, 
            "status", oldStatus != null ? oldStatus.name() : null, newStatus != null ? newStatus.name() : null));
        complaintHistoryRepository.save(history);
    }
    
    /**
     * تسجيل تحديث حقول الشكوى
     */
    public void recordFieldUpdate(Complaint complaint, User actor, String fieldName, String oldValue, String newValue) {
        ComplaintHistory history = new ComplaintHistory(complaint, actor, HistoryActionType.UPDATED_FIELDS);
        history.setFieldChanged(fieldName);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        history.setActionDescription(generateActionDescription(HistoryActionType.UPDATED_FIELDS, actor, 
            fieldName, oldValue, newValue));
        complaintHistoryRepository.save(history);
    }
    
    /**
     * تسجيل إضافة مرفق
     */
    public void recordAttachmentAdded(Complaint complaint, User actor, String fileName, String filePath) {
        ComplaintHistory history = new ComplaintHistory(complaint, actor, HistoryActionType.ATTACHMENT_ADDED);
        
        Map<String, String> metadata = new HashMap<>();
        metadata.put("fileName", fileName);
        metadata.put("filePath", filePath);
        
        try {
            history.setMetadata(objectMapper.writeValueAsString(metadata));
        } catch (JsonProcessingException e) {
            logger.error("Error serializing metadata for attachment", e);
        }
        
        history.setActionDescription(generateActionDescription(HistoryActionType.ATTACHMENT_ADDED, actor, 
            null, null, fileName));
        complaintHistoryRepository.save(history);
    }
    
    /**
     * تسجيل حذف مرفق
     */
    public void recordAttachmentRemoved(Complaint complaint, User actor, String fileName) {
        ComplaintHistory history = new ComplaintHistory(complaint, actor, HistoryActionType.ATTACHMENT_REMOVED);
        
        Map<String, String> metadata = new HashMap<>();
        metadata.put("fileName", fileName);
        
        try {
            history.setMetadata(objectMapper.writeValueAsString(metadata));
        } catch (JsonProcessingException e) {
            logger.error("Error serializing metadata for attachment removal", e);
        }
        
        history.setActionDescription(generateActionDescription(HistoryActionType.ATTACHMENT_REMOVED, actor, 
            null, fileName, null));
        complaintHistoryRepository.save(history);
    }
    
    /**
     * تسجيل حجز الشكوى (State-Based Lock - IN_PROGRESS)
     */
    public void recordLocked(Complaint complaint, User actor) {
        ComplaintHistory history = new ComplaintHistory(complaint, actor, HistoryActionType.LOCKED);
        history.setActionDescription(generateActionDescription(HistoryActionType.LOCKED, actor, null, null, null));
        complaintHistoryRepository.save(history);
    }
    
    /**
     * تسجيل تحرير الشكوى (State-Based Lock - RESOLVED/REJECTED/CLOSED)
     */
    public void recordUnlocked(Complaint complaint, User actor) {
        ComplaintHistory history = new ComplaintHistory(complaint, actor, HistoryActionType.UNLOCKED);
        history.setActionDescription(generateActionDescription(HistoryActionType.UNLOCKED, actor, null, null, null));
        complaintHistoryRepository.save(history);
    }
    
    /**
     * توليد وصف الإجراء بالعربية
     */
    private String generateActionDescription(HistoryActionType actionType, User actor, 
                                             String fieldName, String oldValue, String newValue) {
        String actorName = actor.getFirstName() + " " + actor.getLastName();
        
        switch (actionType) {
            case CREATED:
                return String.format("تم إنشاء الشكوى من قبل %s", actorName);
                
            case STATUS_CHANGED:
                String oldStatusLabel = oldValue != null ? translateStatus(oldValue) : "غير محدد";
                String newStatusLabel = newValue != null ? translateStatus(newValue) : "غير محدد";
                return String.format("تم تغيير حالة الشكوى من '%s' إلى '%s' من قبل %s", 
                    oldStatusLabel, newStatusLabel, actorName);
                
            case UPDATED_FIELDS:
                String fieldLabel = translateFieldName(fieldName);
                return String.format("تم تحديث حقل '%s' من قبل %s", fieldLabel, actorName);
                
            case ATTACHMENT_ADDED:
                return String.format("تم إضافة مرفق '%s' من قبل %s", newValue, actorName);
                
            case ATTACHMENT_REMOVED:
                return String.format("تم حذف مرفق '%s' من قبل %s", oldValue, actorName);
                
            case LOCKED:
                return String.format("تم حجز الشكوى (بدء المعالجة) من قبل %s", actorName);
                
            case UNLOCKED:
                return String.format("تم تحرير الشكوى (انتهاء المعالجة) من قبل %s", actorName);
                
            default:
                return String.format("تم تنفيذ إجراء '%s' من قبل %s", actionType.getLabel(), actorName);
        }
    }
    
    /**
     * ترجمة حالة الشكوى للعربية
     */
    private String translateStatus(String status) {
        if (status == null) return "غير محدد";
        
        switch (status) {
            case "PENDING": return "قيد الانتظار";
            case "IN_PROGRESS": return "قيد المعالجة";
            case "RESOLVED": return "تم الحل";
            case "REJECTED": return "مرفوض";
            case "CLOSED": return "مغلق";
            default: return status;
        }
    }
    
    /**
     * ترجمة اسم الحقل للعربية
     */
    private String translateFieldName(String fieldName) {
        if (fieldName == null) return "حقل غير محدد";
        
        switch (fieldName.toLowerCase()) {
            case "description": return "الوصف";
            case "location": return "الموقع";
            case "complainttype": return "نوع الشكوى";
            case "governorate": return "المحافظة";
            case "governmentagency": return "الجهة الحكومية";
            case "solution_suggestion": return "اقتراح الحل";
            default: return fieldName;
        }
    }
}

