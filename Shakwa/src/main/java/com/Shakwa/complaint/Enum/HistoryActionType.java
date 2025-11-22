package com.Shakwa.complaint.Enum;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * أنواع الإجراءات التي يتم تسجيلها في سجل الشكوى
 */
public enum HistoryActionType {
    
    /**
     * إنشاء الشكوى
     */
    CREATED,
    
    /**
     * تغيير حالة الشكوى
     */
    STATUS_CHANGED,
    
    /**
     * تحديث حقول الشكوى
     */
    UPDATED_FIELDS,
    
    /**
     * إضافة مرفق
     */
    ATTACHMENT_ADDED,
    
    /**
     * حذف مرفق
     */
    ATTACHMENT_REMOVED,
    
    /**
     * إضافة تعليق (للمستقبل)
     */
    COMMENT_ADDED,
    
    /**
     * حجز الشكوى (State-Based Lock - IN_PROGRESS)
     */
    LOCKED,
    
    /**
     * تحرير الشكوى (State-Based Lock - RESOLVED/REJECTED/CLOSED)
     */
    UNLOCKED;

    @JsonValue
    public String getLabel() {
        return this.name().replace("_", " ");
    }
}

