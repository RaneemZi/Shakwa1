package com.Shakwa.complaint.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.Shakwa.complaint.storage.AttachmentStorageService.StoredFile;
import com.Shakwa.utils.exception.ResourceNotFoundException;

@Service
public class LocalAttachmentStorageService implements AttachmentStorageService {

    private final ComplaintStorageProperties properties;

    @Autowired
    public LocalAttachmentStorageService(ComplaintStorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public StoredFile store(MultipartFile file, String trackingNumber) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store an empty file");
        }
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "attachment");
        String extension = "";
        int idx = originalFilename.lastIndexOf('.');
        if (idx >= 0) {
            extension = originalFilename.substring(idx);
        }
        String storedFilename = UUID.randomUUID() + extension;
        Path destinationFolder = properties.getRoot().resolve(trackingNumber).normalize();
        Path destinationFile = destinationFolder.resolve(storedFilename).normalize();
        try {
            Files.createDirectories(destinationFolder);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
            String checksum = calculateChecksum(destinationFile);
            return new StoredFile(storedFilename,
                    properties.getRoot().relativize(destinationFile).toString(),
                    checksum,
                    file.getSize(),
                    file.getContentType());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store file " + originalFilename, e);
        }
    }

    @Override
    public Resource loadAsResource(String location) {
        Path file = properties.getRoot().resolve(location).normalize();
        if (!Files.exists(file)) {
            throw new ResourceNotFoundException("Attachment not found");
        }
        return new FileSystemResource(file.toFile());
    }

    @Override
    public void delete(String location) {
        if (location == null) {
            return;
        }
        Path file = properties.getRoot().resolve(location).normalize();
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to delete attachment " + location, e);
        }
    }

    private String calculateChecksum(Path file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = Files.readAllBytes(file);
            byte[] hash = digest.digest(bytes);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}


