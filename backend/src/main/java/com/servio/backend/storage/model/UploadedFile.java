package com.servio.backend.storage.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UploadedFile {
    private final String fileName;
    private final String fullPath;
    private final String url;
    private final String contentType;
    private final long size;
}