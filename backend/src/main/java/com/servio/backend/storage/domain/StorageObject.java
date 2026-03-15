package com.servio.backend.storage.domain;

public class StorageObject {

    private final String fileName;
    private final String fullPath;
    private final String url;
    private final String contentType;
    private final long size;

    private StorageObject(Builder builder) {
        this.fileName = builder.fileName;
        this.fullPath = builder.fullPath;
        this.url = builder.url;
        this.contentType = builder.contentType;
        this.size = builder.size;
    }

    public String getFileName() { return fileName; }
    public String getFullPath() { return fullPath; }
    public String getUrl() { return url; }
    public String getContentType() { return contentType; }
    public long getSize() { return size; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String fileName;
        private String fullPath;
        private String url;
        private String contentType;
        private long size;

        public Builder fileName(String fileName) { this.fileName = fileName; return this; }
        public Builder fullPath(String fullPath) { this.fullPath = fullPath; return this; }
        public Builder url(String url) { this.url = url; return this; }
        public Builder contentType(String contentType) { this.contentType = contentType; return this; }
        public Builder size(long size) { this.size = size; return this; }

        public StorageObject build() { return new StorageObject(this); }
    }
}