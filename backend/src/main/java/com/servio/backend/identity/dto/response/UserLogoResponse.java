package com.servio.backend.identity.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserLogoResponse {
    private Long id;
    private String url;
    private boolean active;
    private LocalDateTime uploadedAt;
}