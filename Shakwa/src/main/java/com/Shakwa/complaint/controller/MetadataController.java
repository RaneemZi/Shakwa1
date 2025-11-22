package com.Shakwa.complaint.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Shakwa.complaint.Enum.ComplaintType;
import com.Shakwa.complaint.Enum.Governorate;
import com.Shakwa.complaint.service.MetadataService;
import com.Shakwa.user.Enum.GovernmentAgencyType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller for exposing reference data (metadata) endpoints.
 * All endpoints are cached for optimal performance.
 */
@RestController
@RequestMapping("api/v1/metadata")
@Tag(name = "Metadata", description = "APIs for retrieving reference data (enums, types, etc.)")
public class MetadataController {

    private final MetadataService metadataService;

    public MetadataController(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @GetMapping("/complaint-types")
    @Operation(summary = "Get all complaint types", description = "Retrieve all available complaint types. Cached for performance.")
    public ResponseEntity<List<ComplaintType>> getComplaintTypes() {
        return ResponseEntity.ok(metadataService.getAllComplaintTypes());
    }

    @GetMapping("/governorates")
    @Operation(summary = "Get all governorates", description = "Retrieve all available governorates. Cached for performance.")
    public ResponseEntity<List<Governorate>> getGovernorates() {
        return ResponseEntity.ok(metadataService.getAllGovernorates());
    }

    @GetMapping("/government-agencies")
    @Operation(summary = "Get all government agencies", description = "Retrieve all available government agency types. Cached for performance.")
    public ResponseEntity<List<GovernmentAgencyType>> getGovernmentAgencies() {
        return ResponseEntity.ok(metadataService.getAllGovernmentAgencies());
    }

    @GetMapping("/complaint-types/labels")
    @Operation(summary = "Get complaint type labels", description = "Retrieve complaint types as display labels. Cached for performance.")
    public ResponseEntity<List<String>> getComplaintTypeLabels() {
        return ResponseEntity.ok(metadataService.getComplaintTypeLabels());
    }

    @GetMapping("/governorates/labels")
    @Operation(summary = "Get governorate labels", description = "Retrieve governorates as display labels. Cached for performance.")
    public ResponseEntity<List<String>> getGovernorateLabels() {
        return ResponseEntity.ok(metadataService.getGovernorateLabels());
    }

    @GetMapping("/government-agencies/labels")
    @Operation(summary = "Get government agency labels", description = "Retrieve government agencies as display labels. Cached for performance.")
    public ResponseEntity<List<String>> getGovernmentAgencyLabels() {
        return ResponseEntity.ok(metadataService.getGovernmentAgencyLabels());
    }
}

