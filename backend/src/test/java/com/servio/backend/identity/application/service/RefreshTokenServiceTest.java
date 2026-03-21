package com.servio.backend.identity.application.service;

import com.servio.backend.identity.application.port.out.TokenBlacklistPort;
import com.servio.backend.identity.application.port.out.TokenProviderPort;
import com.servio.backend.identity.application.port.out.UserRepositoryPort;
import com.servio.backend.identity.domain.Role;
import com.servio.backend.identity.domain.Token;
import com.servio.backend.identity.domain.User;
import com.servio.backend.identity.domain.exception.AccountBlockedException;
import com.servio.backend.identity.domain.exception.InvalidTokenException;
import com.servio.backend.identity.domain.exception.TokenExpiredException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock private TokenProviderPort tokenProviderPort;
    @Mock private TokenBlacklistPort tokenBlacklistPort;
    @Mock private UserRepositoryPort userRepositoryPort;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    void debeRefrescarTokenCorrectamente() {
        User user = buildUser(false);
        when(tokenBlacklistPort.isRevoked("refresh.token")).thenReturn(false);
        when(tokenProviderPort.extractEmail("refresh.token")).thenReturn("juan@test.com");
        when(tokenProviderPort.isTokenValid("refresh.token", "juan@test.com")).thenReturn(true);
        when(userRepositoryPort.findByEmail("juan@test.com")).thenReturn(Optional.of(user));
        when(tokenProviderPort.generateAccessToken(any(), any())).thenReturn("new.access.token");

        Token token = refreshTokenService.refresh("refresh.token");

        assertThat(token.getAccessToken()).isEqualTo("new.access.token");
        assertThat(token.getRefreshToken()).isEqualTo("refresh.token");
    }

    @Test
    void debeFallarSiTokenRevocado() {
        when(tokenBlacklistPort.isRevoked("refresh.token")).thenReturn(true);

        assertThatThrownBy(() -> refreshTokenService.refresh("refresh.token"))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void debeFallarSiTokenExpirado() {
        when(tokenBlacklistPort.isRevoked("refresh.token")).thenReturn(false);
        when(tokenProviderPort.extractEmail("refresh.token")).thenReturn("juan@test.com"); // ← email sí se extrae
        when(tokenProviderPort.isTokenValid("refresh.token", "juan@test.com")).thenReturn(false); // ← pero el token no es válido

        assertThatThrownBy(() -> refreshTokenService.refresh("refresh.token"))
                .isInstanceOf(TokenExpiredException.class);
    }

    @Test
    void debeFallarSiCuentaBloqueada() {
        User user = buildUser(true);
        when(tokenBlacklistPort.isRevoked("refresh.token")).thenReturn(false);
        when(tokenProviderPort.extractEmail("refresh.token")).thenReturn("juan@test.com");
        when(tokenProviderPort.isTokenValid("refresh.token", "juan@test.com")).thenReturn(true);
        when(userRepositoryPort.findByEmail("juan@test.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> refreshTokenService.refresh("refresh.token"))
                .isInstanceOf(AccountBlockedException.class);
    }

    private User buildUser(boolean blocked) {
        return User.builder()
                .id(1).firstname("Juan").lastname("Perez")
                .email("juan@test.com").password("encoded")
                .role(Role.USER).blocked(blocked)
                .build();
    }
}