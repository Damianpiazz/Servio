package com.servio.backend.identity.dto.request;

import com.servio.backend.identity.model.Role;
import lombok.Data;

@Data
public class AdminUserFilterRequest {
    private String email;
    private String firstname;
    private String lastname;
    private Role role;
    private Boolean blocked;
}