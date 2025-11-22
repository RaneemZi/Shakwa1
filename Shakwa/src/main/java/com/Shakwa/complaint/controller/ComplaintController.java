package com.Shakwa.complaint.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.Shakwa.complaint.Enum.ComplaintStatus;
import com.Shakwa.complaint.Enum.ComplaintType;
import com.Shakwa.complaint.Enum.Governorate;
import com.Shakwa.complaint.dto.ComplaintDTORequest;
import com.Shakwa.complaint.dto.ComplaintDTOResponse;
import com.Shakwa.complaint.dto.ComplaintHistoryDTO;
import com.Shakwa.complaint.service.ComplaintService;
import com.Shakwa.user.Enum.GovernmentAgencyType;
import com.Shakwa.user.dto.PaginationDTO;
import com.Shakwa.utils.response.FileDownloadResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("api/v1/complaints")
@Tag(name = "Complaint Management", description = "APIs for managing complaints")
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @GetMapping
    @Operation(summary = "Get all complaints", description = "Retrieve all complaints based on user role (employee sees only their agency's complaints) with pagination")
    public ResponseEntity<PaginationDTO<ComplaintDTOResponse>> getAllComplaints(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        PaginationDTO<ComplaintDTOResponse> complaints = complaintService.getAllComplaints(page, size);

        return ResponseEntity.ok(complaints);
    }

    @GetMapping("{id}")
    @Operation(summary = "Get complaint by ID", description = "Retrieve a specific complaint by ID")
    public ResponseEntity<ComplaintDTOResponse> getComplaintById(
            @Parameter(description = "Complaint ID", example = "1") 
            @PathVariable Long id) {
        ComplaintDTOResponse complaint = complaintService.getComplaintById(id);
        return ResponseEntity.ok(complaint);
    }

    @GetMapping("citizen/{citizenId}")
    @Operation(summary = "Get complaints by citizen ID", description = "Retrieve all complaints for a specific citizen with pagination")
    public ResponseEntity<PaginationDTO<ComplaintDTOResponse>> getComplaintsByCitizenId(
            @Parameter(description = "Citizen ID", example = "1") 
            @PathVariable Long citizenId,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        PaginationDTO<ComplaintDTOResponse> complaints = complaintService.getComplaintsByCitizenId(citizenId, page, size);
        return ResponseEntity.ok(complaints);
    }

    @GetMapping("status/{status}")
    @Operation(summary = "Get complaints by status", description = "Retrieve all complaints with a specific status with pagination")
    public ResponseEntity<PaginationDTO<ComplaintDTOResponse>> getComplaintsByStatus(
            @Parameter(description = "Complaint status", example = "PENDING") 
            @PathVariable ComplaintStatus status,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        PaginationDTO<ComplaintDTOResponse> complaints = complaintService.getComplaintsByStatus(status, page, size);
        return ResponseEntity.ok(complaints);
    }

    @GetMapping("type/{complaintType}")
    @Operation(summary = "Get complaints by type", description = "Retrieve all complaints of a specific type with pagination")
    public ResponseEntity<PaginationDTO<ComplaintDTOResponse>> getComplaintsByType(
            @Parameter(description = "Complaint type", example = "تأخر_في_إنجاز_معاملة") 
            @PathVariable ComplaintType complaintType,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        PaginationDTO<ComplaintDTOResponse> complaints = complaintService.getComplaintsByType(complaintType, page, size);
        return ResponseEntity.ok(complaints);
    }

    @GetMapping("governorate/{governorate}")
    @Operation(summary = "Get complaints by governorate", description = "Retrieve all complaints from a specific governorate with pagination")
    public ResponseEntity<PaginationDTO<ComplaintDTOResponse>> getComplaintsByGovernorate(
            @Parameter(description = "Governorate", example = "دمشق") 
            @PathVariable Governorate governorate,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        PaginationDTO<ComplaintDTOResponse> complaints = complaintService.getComplaintsByGovernorate(governorate, page, size);
        return ResponseEntity.ok(complaints);
    }

    @GetMapping("filter")
    @Operation(
        summary = "Filter complaints", 
        description = "Filter complaints by multiple criteria (status, type, governorate, government agency, citizen ID) with pagination. All filters are optional."
    )
    public ResponseEntity<PaginationDTO<ComplaintDTOResponse>> filterComplaints(
            @Parameter(description = "Complaint status", example = "PENDING")
            @RequestParam(required = false) ComplaintStatus status,
            @Parameter(description = "Complaint type", example = "تأخر_في_إنجاز_معاملة")
            @RequestParam(required = false) ComplaintType complaintType,
            @Parameter(description = "Governorate", example = "دمشق")
            @RequestParam(required = false) Governorate governorate,
            @Parameter(description = "Government agency", example = "وزارة_الصحة")
            @RequestParam(required = false) GovernmentAgencyType governmentAgency,
            @Parameter(description = "Citizen ID", example = "1")
            @RequestParam(required = false) Long citizenId,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        PaginationDTO<ComplaintDTOResponse> complaints = complaintService.filterComplaints(
            status, complaintType, governorate, governmentAgency, citizenId, page, size);
        return ResponseEntity.ok(complaints);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Create complaint", 
        description = "Create a new complaint. Only citizens can create complaints about government services. The citizen will be identified from the authentication token."
    )
    public ResponseEntity<ComplaintDTOResponse> createComplaint(
            @Parameter(description = "Complaint data", required = true)
            @RequestPart("data") ComplaintDTORequest dto,
            @Parameter(description = "Attachment files", required = false)
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        ComplaintDTOResponse complaint = complaintService.createComplaint(dto, files);
        return ResponseEntity.ok(complaint);
    }

    @PostMapping(value = "{id}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Add attachments to complaint", description = "Allows the complaint owner to upload additional files")
    public ResponseEntity<ComplaintDTOResponse> addAttachments(
            @PathVariable Long id,
            @RequestPart("files") List<MultipartFile> files) {
        return ResponseEntity.ok(complaintService.addAttachments(id, files));
    }

    @GetMapping("{id}/attachments/{attachmentId}")
    @Operation(summary = "Download attachment", description = "Download a single attachment if authorized")
    public ResponseEntity<?> downloadAttachment(
            @PathVariable Long id,
            @PathVariable Long attachmentId) {
        FileDownloadResponse download = complaintService.downloadAttachment(id, attachmentId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + download.filename() + "\"")
                .contentType(MediaType.parseMediaType(download.contentType()))
                .contentLength(download.size())
                .body(download.resource());
    }

    @DeleteMapping("{id}/attachments/{attachmentId}")
    @Operation(summary = "Delete attachment", description = "Remove an attachment from a complaint")
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable Long id,
            @PathVariable Long attachmentId) {
        complaintService.deleteAttachment(id, attachmentId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("{id}")
    @Operation(summary = "Update complaint", description = "Update an existing complaint. Only employees from the same agency can update.")
    public ResponseEntity<ComplaintDTOResponse> updateComplaint(
            @Parameter(description = "Complaint ID", example = "1") 
            @PathVariable Long id,
            @Parameter(description = "Updated complaint data", required = true)
            @RequestBody ComplaintDTORequest dto) {
        ComplaintDTOResponse complaint = complaintService.updateComplaint(id, dto);
        return ResponseEntity.ok(complaint);
    }

    @PutMapping("{id}/respond")
    @Operation(
        summary = "Respond to complaint", 
        description = "Respond to a complaint and update its status. Only employees can respond."
    )
    public ResponseEntity<ComplaintDTOResponse> respondToComplaint(
            @Parameter(description = "Complaint ID", example = "1") 
            @PathVariable Long id,
            @Parameter(description = "Response text", required = true)
            @RequestParam String response,
            @Parameter(description = "New status (optional)", example = "RESOLVED")
            @RequestParam(required = false) ComplaintStatus status) {
        ComplaintDTOResponse complaint = complaintService.respondToComplaint(id, response, status);
        return ResponseEntity.ok(complaint);
    }

    @DeleteMapping("{id}")
    @Operation(summary = "Delete complaint", description = "Delete a complaint. Only authorized users can delete.")
    public ResponseEntity<Void> deleteComplaint(
            @Parameter(description = "Complaint ID", example = "1") 
            @PathVariable Long id) {
        complaintService.deleteComplaint(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("{id}/history")
    @Operation(
        summary = "Get complaint history", 
        description = "Retrieve the complete history timeline of a complaint. Citizens see only their own complaints, employees see their agency's complaints, admins see all. Returns paginated list ordered chronologically (newest first)."
    )
    public ResponseEntity<PaginationDTO<ComplaintHistoryDTO>> getComplaintHistory(
            @Parameter(description = "Complaint ID", example = "1") 
            @PathVariable Long id,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        PaginationDTO<ComplaintHistoryDTO> history = complaintService.getComplaintHistory(id, page, size);
        return ResponseEntity.ok(history);
    }

}

