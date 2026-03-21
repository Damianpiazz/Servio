package com.servio.backend.identity.application.port.in.usecase;

import com.servio.backend.identity.domain.Token;

public interface RefreshTokenUseCase {
    Token refresh(String refreshToken);
}