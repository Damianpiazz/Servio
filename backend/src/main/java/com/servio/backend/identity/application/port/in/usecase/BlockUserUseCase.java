package com.servio.backend.identity.application.port.in.usecase;

public interface BlockUserUseCase {
    void block(Integer userId);
    void unblock(Integer userId);
}