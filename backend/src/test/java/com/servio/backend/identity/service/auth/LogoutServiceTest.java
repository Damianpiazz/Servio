package com.servio.backend.identity.service.auth;

import com.servio.backend.identity.service.blacklist.TokenBlacklistService;
import com.servio.backend.identity.service.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {

    @Mock private TokenBlacklistService tokenBlacklistService;
    @Mock private JwtService jwtService;

    @InjectMocks private LogoutService logoutService;

    @Test
    void logout_shouldRevokeJtiWithCorrectTtl() {
        when(jwtService.extractJti("some-token")).thenReturn("jti-abc");
        when(jwtService.getAccessTokenTtlSeconds()).thenReturn(86400L);

        logoutService.logout("some-token");

        verify(tokenBlacklistService).revoke("jti-abc", 86400L);
    }

    @Test
    void logout_shouldExtractJtiFromToken() {
        when(jwtService.extractJti("token-xyz")).thenReturn("jti-xyz");
        when(jwtService.getAccessTokenTtlSeconds()).thenReturn(3600L);

        logoutService.logout("token-xyz");

        verify(jwtService).extractJti("token-xyz");
    }
}