package com.servio.backend.identity.application.port.in.command;

import com.servio.backend.identity.domain.Role;

public record RegisterCommand(
        String firstname,
        String lastname,
        String email,
        String password,
        Role role
) {}