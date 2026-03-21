package com.servio.backend.identity.infrastructure.web.dto;

import com.servio.backend.identity.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Integer id;
    private String firstname;
    private String lastname;
    private String email;
    private Role role;
    private boolean blocked;
    private String activeLogoUrl;
}