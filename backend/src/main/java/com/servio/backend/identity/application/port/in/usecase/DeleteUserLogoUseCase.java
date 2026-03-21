package com.servio.backend.identity.application.port.in.usecase;

import com.servio.backend.identity.domain.User;

public interface DeleteUserLogoUseCase {
    void delete(Long logoId, User user);
}