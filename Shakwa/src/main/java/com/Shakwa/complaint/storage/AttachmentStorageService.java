package com.Shakwa.complaint.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface AttachmentStorageService {

    StoredFile store(MultipartFile file, String trackingNumber);

    Resource loadAsResource(String location);

    void delete(String location);

    record StoredFile(String storedFilename, String relativePath, String checksum, long size, String contentType) {}
}


