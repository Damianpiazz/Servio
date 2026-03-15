package com.servio.backend.storage.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectStorageConfig {

    @Value("${object-storage.endpoint}")
    private String endpoint;

    @Value("${object-storage.access-key}")
    private String accessKey;

    @Value("${object-storage.secret-key}")
    private String secretKey;

    @Value("${object-storage.bucket}")
    private String bucket;

    public String getEndpoint() { return endpoint; }
    public String getAccessKey() { return accessKey; }
    public String getSecretKey() { return secretKey; }
    public String getBucket() { return bucket; }
}