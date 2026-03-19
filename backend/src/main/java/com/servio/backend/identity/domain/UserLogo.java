package com.servio.backend.identity.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserLogo {
    private Long id;
    private Integer userId;
    private String url;
    private String fullPath;
    private boolean active;
    private LocalDateTime uploadedAt;
}