package com.Shakwa.complaint.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.Shakwa.complaint.Enum.ComplaintStatus;
import com.Shakwa.complaint.Enum.ComplaintType;
import com.Shakwa.complaint.Enum.Governorate;
import com.Shakwa.user.Enum.GovernmentAgencyType;
import com.Shakwa.user.entity.Citizen;
import com.Shakwa.user.entity.Employee;
import com.Shakwa.utils.entity.AuditedEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
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
public class Complaint extends AuditedEntity {
    
    @Override
    protected String getSequenceName() {
        return "complaint_id_seq";
    }
    
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

    @Column(name = "tracking_number", nullable = false, unique = true, updatable = false, length = 48)
    private String trackingNumber;

    // Optimistic locking version column
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    // المرفقات
    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComplaintAttachment> attachments = new ArrayList<>();

    // علاقة مع المواطن الذي قدم الشكوى
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citizen_id", nullable = false)
    private Citizen citizen;



}

