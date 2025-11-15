package com.Shakwa.user.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.Shakwa.user.Enum.ComplaintStatus;
import com.Shakwa.user.Enum.ComplaintType;
import com.Shakwa.user.Enum.GovernmentAgencyType;
import com.Shakwa.user.Enum.Governorate;
import com.Shakwa.utils.entity.BaseEntity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Table(name = "complaints")
@NoArgsConstructor
@AllArgsConstructor
@SequenceGenerator(name = "complaint_seq", sequenceName = "complaint_id_seq", allocationSize = 1)
public class Complaint extends BaseEntity {
    
    // نوع الشكوى
    @Enumerated(EnumType.STRING)
    @Column(name = "complaint_type", nullable = false)
    private ComplaintType complaintType;

    // المحافظة
    @Enumerated(EnumType.STRING)
    @Column(name = "governorate", nullable = false)
    private Governorate governorate;

    // الجهة الحكومية
    @Enumerated(EnumType.STRING)
    @Column(name = "government_agency", nullable = false)
    private GovernmentAgencyType governmentAgency;

    // موقع الشكوى - نص تفصيلي
    @Column(name = "location", columnDefinition = "TEXT", nullable = false)
    private String location;

    // وصف الشكوى - نص يصف ما حصل ومتى
    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    // اقتراح حل
    @Column(name = "solution_suggestion", columnDefinition = "TEXT")
    private String solutionSuggestion;

    // حالة الشكوى
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ComplaintStatus status = ComplaintStatus.PENDING;

    // الرد على الشكوى
    @Column(name = "response", columnDefinition = "TEXT")
    private String response;

    // تاريخ الرد
    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    // الموظف الذي رد على الشكوى
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responded_by")
    private Employee respondedBy;

    // المرفقات - قائمة بمسارات الملفات (png أو pdf)
    @ElementCollection
    @CollectionTable(name = "complaint_attachments", joinColumns = @JoinColumn(name = "complaint_id"))
    @Column(name = "file_path")
    private List<String> attachments = new ArrayList<>();

    // علاقة مع المواطن الذي قدم الشكوى
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citizen_id", nullable = false)
    private Citizen citizen;

}

