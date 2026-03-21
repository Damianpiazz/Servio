package com.servio.backend.identity.application.port.in.command;

public record LoginCommand(String email, String password) {}