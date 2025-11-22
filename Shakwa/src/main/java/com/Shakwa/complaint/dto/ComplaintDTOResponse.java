package com.Shakwa.complaint.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.Shakwa.complaint.Enum.ComplaintStatus;
import com.Shakwa.complaint.Enum.ComplaintType;
import com.Shakwa.complaint.Enum.Governorate;
import com.Shakwa.user.Enum.GovernmentAgencyType;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Complaint Response")
public class ComplaintDTOResponse {

    @Schema(description = "Complaint ID", example = "1")
    private Long id;

    @Schema(description = "Type of complaint", example = "تأخر_في_إنجاز_معاملة")
    private ComplaintType complaintType;

    @Schema(description = "Governorate", example = "دمشق")
    private Governorate governorate;

    @Schema(description = "Government agency", example = "وزارة_الصحة")
    private GovernmentAgencyType governmentAgency;

    @Schema(description = "Location of complaint", example = "مكتب الخدمات - دمشق - ساحة الأمويين")
    private String location;

    @Schema(description = "Complaint description", example = "تم التأخر في إنجاز معاملة الحصول على شهادة ميلاد لأكثر من أسبوعين")
    private String description;

    @Schema(description = "Solution suggestion", example = "تحسين إجراءات المعاملة")
    private String solutionSuggestion;

    @Schema(description = "Status of the complaint", example = "PENDING")
    private ComplaintStatus status;

    @Schema(description = "Response to the complaint", example = "تم الاطلاع على الشكوى وسيتم متابعتها")
    private String response;

    @Schema(description = "Date and time when complaint was responded to")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime respondedAt;

    @Schema(description = "ID of employee who responded", example = "1")
    private Long respondedById;

    @Schema(description = "Name of employee who responded", example = "أحمد محمد")
    private String respondedByName;

    @Schema(description = "List of attachment metadata with download links")
    @Builder.Default
    private List<ComplaintAttachmentDTO> attachments = new ArrayList<>();

    @Schema(description = "Citizen ID who submitted the complaint", example = "1")
    private Long citizenId;

    @Schema(description = "Citizen name who submitted the complaint", example = "محمد أحمد")
    private String citizenName;

    @Schema(description = "Date and time when complaint was created")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Date and time when complaint was last updated")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @Schema(description = "Tracking number citizens can use to follow up", example = "SHK-20250215-AB12CD")
    private String trackingNumber;

    @Schema(description = "Version number for optimistic locking", example = "1")
    private Long version;
}

