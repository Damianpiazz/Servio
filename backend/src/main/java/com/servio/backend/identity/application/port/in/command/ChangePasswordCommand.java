package com.servio.backend.identity.application.port.in.command;

public record ChangePasswordCommand(
        String currentPassword,
        String newPassword,
        String confirmationPassword
) {}