package com.servio.backend.identity.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLogoResponse {
    private Long id;
    private String url;
    private boolean active;
    private LocalDateTime uploadedAt;
}