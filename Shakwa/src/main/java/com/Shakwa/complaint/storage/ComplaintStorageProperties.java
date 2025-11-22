package com.Shakwa.complaint.storage;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "storage.complaints")
public class ComplaintStorageProperties {

    /**
     * Root directory where complaint attachments will be stored.
     */
    private Path root = Paths.get("storage/complaints");
}

