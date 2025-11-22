package com.Shakwa.complaint.entity;

import java.time.LocalDateTime;

import com.Shakwa.complaint.Enum.HistoryActionType;
import com.Shakwa.user.entity.User;
import com.Shakwa.utils.entity.AuditedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * سجل تغييرات الشكوى - يحفظ جميع التغييرات التي تحدث على الشكوى
 * Immutable timeline of every change to a complaint
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Table(name = "complaint_history")
@NoArgsConstructor
@AllArgsConstructor
@SequenceGenerator(name = "complaint_history_seq", sequenceName = "complaint_history_id_seq", allocationSize = 1)
public class ComplaintHistory extends AuditedEntity {
    
    @Override
    protected String getSequenceName() {
        return "complaint_history_id_seq";
    }
    
    /**
     * الشكوى المرتبطة بهذا السجل
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;
    
    /**
     * المستخدم الذي قام بالإجراء (actor)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;
    
    /**
     * نوع الإجراء
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private HistoryActionType actionType;
    
    /**
     * الحقل الذي تم تغييره (nullable - مثل "status", "description", etc.)
     */
    @Column(name = "field_changed")
    private String fieldChanged;
    
    /**
     * القيمة القديمة
     */
    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;
    
    /**
     * القيمة الجديدة
     */
    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;
    
    /**
     * بيانات إضافية بصيغة JSON (مثل معلومات المرفق، تفاصيل التغيير، etc.)
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
    
    /**
     * وصف الإجراء بالعربية (generated from template)
     */
    @Column(name = "action_description", columnDefinition = "TEXT")
    private String actionDescription;
    
    /**
     * Constructor helper for creating history entries
     * Note: createdAt is inherited from AuditedEntity and will be set automatically
     */
    public ComplaintHistory(Complaint complaint, User actor, HistoryActionType actionType) {
        this.complaint = complaint;
        this.actor = actor;
        this.actionType = actionType;
    }
}

