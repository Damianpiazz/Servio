package com.servio.backend.identity.dto.request;

import com.servio.backend.identity.model.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeUserRoleRequest {

    @NotNull(message = "Role is required")
    private Role role;
}