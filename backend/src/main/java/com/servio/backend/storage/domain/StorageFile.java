package com.servio.backend.storage.domain;

public class StorageFile {

    private final String fileName;
    private final byte[] content;
    private final String contentType;
    private final long size;
    private final String folder;

    private StorageFile(Builder builder) {
        this.fileName = builder.fileName;
        this.content = builder.content;
        this.contentType = builder.contentType;
        this.size = builder.size;
        this.folder = builder.folder != null ? builder.folder : "";
    }

    public String getFileName() { return fileName; }
    public byte[] getContent() { return content; }
    public String getContentType() { return contentType; }
    public long getSize() { return size; }
    public String getFolder() { return folder; }

    public String getFullPath() {
        return folder.isBlank() ? fileName : folder + "/" + fileName;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String fileName;
        private byte[] content;
        private String contentType;
        private long size;
        private String folder;

        public Builder fileName(String fileName) { this.fileName = fileName; return this; }
        public Builder content(byte[] content) { this.content = content; return this; }
        public Builder contentType(String contentType) { this.contentType = contentType; return this; }
        public Builder size(long size) { this.size = size; return this; }
        public Builder folder(String folder) { this.folder = folder; return this; }

        public StorageFile build() {
            if (fileName == null || fileName.isBlank()) throw new IllegalArgumentException("El nombre del archivo es obligatorio");
            if (content == null || content.length == 0) throw new IllegalArgumentException("El contenido del archivo es obligatorio");
            if (contentType == null || contentType.isBlank()) throw new IllegalArgumentException("El tipo de contenido es obligatorio");
            return new StorageFile(this);
        }
    }
}