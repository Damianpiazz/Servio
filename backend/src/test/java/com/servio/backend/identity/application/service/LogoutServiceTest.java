package com.servio.backend.identity.application.service;

import com.servio.backend.identity.application.port.out.TokenBlacklistPort;
import com.servio.backend.identity.application.port.out.TokenProviderPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {

    @Mock private TokenBlacklistPort tokenBlacklistPort;
    @Mock private TokenProviderPort tokenProviderPort;

    @InjectMocks
    private LogoutService logoutService;

    @Test
    void debeRevocarTokenAlHacerLogout() {
        when(tokenProviderPort.getAccessTokenTtlSeconds()).thenReturn(86400L);

        logoutService.logout("access.token");

        verify(tokenBlacklistPort, times(1)).revoke("access.token", 86400L);
    }
}