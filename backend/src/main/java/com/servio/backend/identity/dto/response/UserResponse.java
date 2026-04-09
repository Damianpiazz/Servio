package com.servio.backend.identity.dto.response;

import com.servio.backend.identity.model.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Integer id;
    private String firstname;
    private String lastname;
    private String email;
    private Role role;
    private boolean blocked;
    private String activeLogoUrl;
}