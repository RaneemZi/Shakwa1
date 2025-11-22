package com.Shakwa.complaint.dto;

import com.Shakwa.complaint.Enum.ComplaintStatus;
import com.Shakwa.complaint.Enum.ComplaintType;
import com.Shakwa.complaint.Enum.Governorate;
import com.Shakwa.user.Enum.GovernmentAgencyType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Complaint Request", example = """
{
  "complaintType": "تأخر في إنجاز معاملة",
  "governorate": "دمشق",
  "governmentAgency": "وزارة الصحة",
  "location": "مكتب الخدمات - دمشق - ساحة الأمويين",
  "description": "تم التأخر في إنجاز معاملة الحصول على شهادة ميلاد لأكثر من أسبوعين",
  "solutionSuggestion": "تحسين إجراءات المعاملة",
  "citizenId": 1
}
""")
public class ComplaintDTORequest {

    @Schema(description = "Type of complaint", example = "تأخر_في_إنجاز_معاملة", required = true)
    @NotNull(message = "Complaint type is required")
    private ComplaintType complaintType;

    @Schema(description = "Governorate", example = "دمشق", required = true)
    @NotNull(message = "Governorate is required")
    private Governorate governorate;

    @Schema(description = "Government agency", example = "وزارة_الصحة", required = true)
    @NotNull(message = "Government agency is required")
    private GovernmentAgencyType governmentAgency;

    @Schema(description = "Location of complaint (detailed text)", example = "مكتب الخدمات - دمشق - ساحة الأمويين", required = true)
    @NotBlank(message = "Location is required")
    private String location;

    @Schema(description = "Complaint description (what happened and when)", example = "تم التأخر في إنجاز معاملة الحصول على شهادة ميلاد لأكثر من أسبوعين", required = true)
    @NotBlank(message = "Description is required")
    private String description;

    @Schema(description = "Solution suggestion", example = "تحسين إجراءات المعاملة")
    private String solutionSuggestion;

    @Schema(description = "Citizen ID who submitted the complaint (optional if current user is a citizen - will be taken from token)", example = "1")
    private Long citizenId;

    @Schema(description = "Status of the complaint", example = "PENDING")
    private ComplaintStatus status;
}

