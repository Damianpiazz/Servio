package com.servio.backend.storage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "object-storage")
public record StorageProperties(
        String endpoint,
        String accessKey,
        String secretKey,
        String bucket
) {}