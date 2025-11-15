package com.Shakwa.user.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Shakwa.user.Enum.ComplaintStatus;
import com.Shakwa.user.Enum.ComplaintType;
import com.Shakwa.user.Enum.Governorate;
import com.Shakwa.user.dto.ComplaintDTORequest;
import com.Shakwa.user.dto.ComplaintDTOResponse;
import com.Shakwa.user.service.ComplaintService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("api/v1/complaints")
@Tag(name = "Complaint Management", description = "APIs for managing complaints")
public class ComplaintController {

    private final ComplaintService complaintService;
    private static final Logger logger = LoggerFactory.getLogger(ComplaintController.class);

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
        logger.info("ComplaintController initialized successfully");
    }

    @GetMapping
    @Operation(summary = "Get all complaints", description = "Retrieve all complaints based on user role (employee sees only their agency's complaints)")
    public ResponseEntity<List<ComplaintDTOResponse>> getAllComplaints() {
        logger.info("Fetching all complaints");
        List<ComplaintDTOResponse> complaints = complaintService.getAllComplaints();
        logger.info("Retrieved {} complaints", complaints.size());
        return ResponseEntity.ok(complaints);
    }

    @GetMapping("{id}")
    @Operation(summary = "Get complaint by ID", description = "Retrieve a specific complaint by ID")
    public ResponseEntity<ComplaintDTOResponse> getComplaintById(
            @Parameter(description = "Complaint ID", example = "1") 
            @PathVariable Long id) {
        logger.info("Fetching complaint with ID: {}", id);
        ComplaintDTOResponse complaint = complaintService.getComplaintById(id);
        logger.info("Retrieved complaint with ID: {}", id);
        return ResponseEntity.ok(complaint);
    }

    @GetMapping("citizen/{citizenId}")
    @Operation(summary = "Get complaints by citizen ID", description = "Retrieve all complaints for a specific citizen")
    public ResponseEntity<List<ComplaintDTOResponse>> getComplaintsByCitizenId(
            @Parameter(description = "Citizen ID", example = "1") 
            @PathVariable Long citizenId) {
        logger.info("Fetching complaints for citizen ID: {}", citizenId);
        List<ComplaintDTOResponse> complaints = complaintService.getComplaintsByCitizenId(citizenId);
        logger.info("Retrieved {} complaints for citizen ID: {}", complaints.size(), citizenId);
        return ResponseEntity.ok(complaints);
    }

    @GetMapping("status/{status}")
    @Operation(summary = "Get complaints by status", description = "Retrieve all complaints with a specific status")
    public ResponseEntity<List<ComplaintDTOResponse>> getComplaintsByStatus(
            @Parameter(description = "Complaint status", example = "PENDING") 
            @PathVariable ComplaintStatus status) {
        logger.info("Fetching complaints with status: {}", status);
        List<ComplaintDTOResponse> complaints = complaintService.getComplaintsByStatus(status);
        logger.info("Retrieved {} complaints with status: {}", complaints.size(), status);
        return ResponseEntity.ok(complaints);
    }

    @GetMapping("type/{complaintType}")
    @Operation(summary = "Get complaints by type", description = "Retrieve all complaints of a specific type")
    public ResponseEntity<List<ComplaintDTOResponse>> getComplaintsByType(
            @Parameter(description = "Complaint type", example = "تأخر_في_إنجاز_معاملة") 
            @PathVariable ComplaintType complaintType) {
        logger.info("Fetching complaints with type: {}", complaintType);
        List<ComplaintDTOResponse> complaints = complaintService.getComplaintsByType(complaintType);
        logger.info("Retrieved {} complaints with type: {}", complaints.size(), complaintType);
        return ResponseEntity.ok(complaints);
    }

    @GetMapping("governorate/{governorate}")
    @Operation(summary = "Get complaints by governorate", description = "Retrieve all complaints from a specific governorate")
    public ResponseEntity<List<ComplaintDTOResponse>> getComplaintsByGovernorate(
            @Parameter(description = "Governorate", example = "دمشق") 
            @PathVariable Governorate governorate) {
        logger.info("Fetching complaints for governorate: {}", governorate);
        List<ComplaintDTOResponse> complaints = complaintService.getComplaintsByGovernorate(governorate);
        logger.info("Retrieved {} complaints for governorate: {}", complaints.size(), governorate);
        return ResponseEntity.ok(complaints);
    }

    @PostMapping
    @Operation(
        summary = "Create complaint", 
        description = "Create a new complaint. Only citizens can create complaints about government services. The citizen will be identified from the authentication token."
    )
    public ResponseEntity<ComplaintDTOResponse> createComplaint(
            @Parameter(description = "Complaint data", required = true)
            @RequestBody ComplaintDTORequest dto) {
        logger.info("Creating new complaint");
        ComplaintDTOResponse complaint = complaintService.createComplaint(dto);
        logger.info("Successfully created complaint with ID: {} for citizen ID: {}", 
                   complaint.getId(), complaint.getCitizenId());
        return ResponseEntity.ok(complaint);
    }

    @PutMapping("{id}")
    @Operation(summary = "Update complaint", description = "Update an existing complaint. Only employees from the same agency can update.")
    public ResponseEntity<ComplaintDTOResponse> updateComplaint(
            @Parameter(description = "Complaint ID", example = "1") 
            @PathVariable Long id,
            @Parameter(description = "Updated complaint data", required = true)
            @RequestBody ComplaintDTORequest dto) {
        logger.info("Updating complaint with ID: {}", id);
        ComplaintDTOResponse complaint = complaintService.updateComplaint(id, dto);
        logger.info("Successfully updated complaint with ID: {}", id);
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
        logger.info("Responding to complaint with ID: {}", id);
        ComplaintDTOResponse complaint = complaintService.respondToComplaint(id, response, status);
        logger.info("Successfully responded to complaint with ID: {}", id);
        return ResponseEntity.ok(complaint);
    }

    @DeleteMapping("{id}")
    @Operation(summary = "Delete complaint", description = "Delete a complaint. Only authorized users can delete.")
    public ResponseEntity<Void> deleteComplaint(
            @Parameter(description = "Complaint ID", example = "1") 
            @PathVariable Long id) {
        logger.info("Deleting complaint with ID: {}", id);
        complaintService.deleteComplaint(id);
        logger.info("Successfully deleted complaint with ID: {}", id);
        return ResponseEntity.ok().build();
    }
}

