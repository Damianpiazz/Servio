package com.servio.backend.identity.application.service;

import com.servio.backend.identity.application.port.in.usecase.LogoutUseCase;
import com.servio.backend.identity.application.port.out.TokenBlacklistPort;
import com.servio.backend.identity.application.port.out.TokenProviderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutUseCase {

    private final TokenBlacklistPort tokenBlacklistPort;
    private final TokenProviderPort tokenProviderPort;

    @Override
    public void logout(String token) {
        long ttl = tokenProviderPort.getAccessTokenTtlSeconds();
        tokenBlacklistPort.revoke(token, ttl);
    }
}