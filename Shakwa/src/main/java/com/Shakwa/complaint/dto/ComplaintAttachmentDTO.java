package com.Shakwa.complaint.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Metadata for an attached file")
public class ComplaintAttachmentDTO {

    @Schema(description = "Attachment ID", example = "42")
    private Long id;

    @Schema(description = "Original file name", example = "evidence.pdf")
    private String originalFilename;

    @Schema(description = "File size in bytes", example = "204800")
    private long size;

    @Schema(description = "MIME type", example = "application/pdf")
    private String contentType;

    @Schema(description = "Download URL for the attachment")
    private String downloadUrl;

    @Schema(description = "When the file was uploaded")
    private LocalDateTime uploadedAt;
}


