package com.Shakwa.complaint.converter;

import com.Shakwa.complaint.Enum.ComplaintStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Converter to handle String to ComplaintStatus enum conversion.
 * Supports both enum name format (IN_PROGRESS) and display format (IN PROGRESS).
 */
@Component
public class StringToComplaintStatusConverter implements Converter<String, ComplaintStatus> {

    @Override
    public ComplaintStatus convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        
        // First, try to match the enum name directly (e.g., "IN_PROGRESS")
        try {
            return ComplaintStatus.valueOf(source.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            // If that fails, try to match by replacing spaces with underscores
            // This handles cases like "IN PROGRESS" -> "IN_PROGRESS"
            String normalized = source.trim().toUpperCase().replace(" ", "_");
            try {
                return ComplaintStatus.valueOf(normalized);
            } catch (IllegalArgumentException ex) {
                // If still not found, try to match by the display label (getLabel() method)
                // This handles reverse lookup: "IN PROGRESS" -> IN_PROGRESS
                for (ComplaintStatus status : ComplaintStatus.values()) {
                    if (status.getLabel().equalsIgnoreCase(source.trim())) {
                        return status;
                    }
                }
                // If nothing matches, throw a more descriptive exception
                throw new IllegalArgumentException(
                    String.format("Invalid complaint status: '%s'. Valid values are: %s", 
                        source, 
                        String.join(", ", 
                            java.util.Arrays.stream(ComplaintStatus.values())
                                .map(ComplaintStatus::getLabel)
                                .toArray(String[]::new)
                        )
                    )
                );
            }
        }
    }
}

