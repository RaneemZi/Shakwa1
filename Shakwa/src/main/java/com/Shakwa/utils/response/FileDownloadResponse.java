package com.Shakwa.utils.response;

import org.springframework.core.io.Resource;

public record FileDownloadResponse(Resource resource, String filename, String contentType, long size) {
}


