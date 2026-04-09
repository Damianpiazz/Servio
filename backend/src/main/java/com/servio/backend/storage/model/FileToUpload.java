package com.servio.backend.storage.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class FileToUpload {

    private final String fileName;
    private final byte[] content;
    private final String contentType;
    private final long size;
    private final String folder;

    public String getFullPath() {
        return (folder == null || folder.isBlank())
                ? fileName
                : folder + "/" + fileName;
    }

    public static FileToUpload of(
            String fileName,
            byte[] content,
            String contentType,
            long size,
            String folder
    ) {
        return FileToUpload.builder()
                .fileName(fileName)
                .content(content)
                .contentType(contentType)
                .size(size)
                .folder(folder != null ? folder : "")
                .build();
    }
}