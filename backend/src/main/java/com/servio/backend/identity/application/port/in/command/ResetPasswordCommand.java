package com.servio.backend.identity.application.port.in.command;

public record ResetPasswordCommand(String token, String newPassword) {}