package com.servio.backend.identity.application.port.in.usecase;

public interface LogoutUseCase {
    void logout(String token);
}