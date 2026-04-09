package com.servio.backend.identity.service.auth;

import com.servio.backend.identity.dto.response.AuthResponse;
import com.servio.backend.identity.exception.AccountBlockedException;
import com.servio.backend.identity.exception.InvalidTokenException;
import com.servio.backend.identity.exception.TokenExpiredException;
import com.servio.backend.identity.model.Role;
import com.servio.backend.identity.model.User;
import com.servio.backend.identity.repository.UserRepository;
import com.servio.backend.identity.service.blacklist.TokenBlacklistService;
import com.servio.backend.identity.service.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock private JwtService jwtService;
    @Mock private TokenBlacklistService tokenBlacklistService;
    @Mock private UserRepository userRepository;

    @InjectMocks private RefreshTokenService refreshTokenService;

    // ─── happy path ───────────────────────────────────────────────

    @Test
    void refresh_shouldReturnNewTokens_whenRefreshTokenIsValid() {
        User user = activeUser();
        setupValidRefreshToken("refresh-token", "john@example.com");
        when(jwtService.extractTokenVersion("refresh-token")).thenReturn(0);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(any(), any(), anyInt())).thenReturn("new-access");
        when(jwtService.generateRefreshToken(any(), anyInt())).thenReturn("new-refresh");

        AuthResponse response = refreshTokenService.refresh("refresh-token");

        assertThat(response.getAccessToken()).isEqualTo("new-access");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh");
    }

    @Test
    void refresh_shouldRevokeOldRefreshToken() {
        User user = activeUser();
        setupValidRefreshToken("old-refresh", "john@example.com");
        when(jwtService.extractTokenVersion("old-refresh")).thenReturn(0);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(any(), any(), anyInt())).thenReturn("a");
        when(jwtService.generateRefreshToken(any(), anyInt())).thenReturn("r");
        when(jwtService.getRefreshTokenTtlSeconds()).thenReturn(604800L);

        refreshTokenService.refresh("old-refresh");

        verify(tokenBlacklistService).revoke("jti-123", 604800L);
    }

    // ─── revoked token ────────────────────────────────────────────

    @Test
    void refresh_shouldThrow_whenTokenIsRevoked() {
        when(jwtService.extractJti("revoked-token")).thenReturn("jti-revoked");
        when(tokenBlacklistService.isRevoked("jti-revoked")).thenReturn(true);

        assertThatThrownBy(() -> refreshTokenService.refresh("revoked-token"))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void refresh_shouldThrow_whenJtiIsNull() {
        when(jwtService.extractJti("bad-token")).thenReturn(null);

        assertThatThrownBy(() -> refreshTokenService.refresh("bad-token"))
                .isInstanceOf(InvalidTokenException.class);
    }

    // ─── not a refresh token ──────────────────────────────────────

    @Test
    void refresh_shouldThrow_whenTokenIsAccessType() {
        when(jwtService.extractJti("access-token")).thenReturn("jti-acc");
        when(tokenBlacklistService.isRevoked("jti-acc")).thenReturn(false);
        when(jwtService.isRefreshToken("access-token")).thenReturn(false);

        assertThatThrownBy(() -> refreshTokenService.refresh("access-token"))
                .isInstanceOf(InvalidTokenException.class);
    }

    // ─── expired token ────────────────────────────────────────────

    @Test
    void refresh_shouldThrow_whenTokenIsExpired() {
        when(jwtService.extractJti("expired-token")).thenReturn("jti-exp");
        when(tokenBlacklistService.isRevoked("jti-exp")).thenReturn(false);
        when(jwtService.isRefreshToken("expired-token")).thenReturn(true);
        when(jwtService.extractEmail("expired-token")).thenReturn("john@example.com");
        when(jwtService.isTokenValid("expired-token", "john@example.com")).thenReturn(false);

        assertThatThrownBy(() -> refreshTokenService.refresh("expired-token"))
                .isInstanceOf(TokenExpiredException.class);
    }

    // ─── blocked user ─────────────────────────────────────────────

    @Test
    void refresh_shouldThrow_whenUserIsBlocked() {
        String refreshToken = "refresh-token";
        String email = "blocked@example.com";

        setupValidRefreshToken(refreshToken, email);

        when(jwtService.extractEmail(refreshToken))
                .thenReturn(email);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(blockedUser()));

        assertThatThrownBy(() -> refreshTokenService.refresh(refreshToken))
                .isInstanceOf(AccountBlockedException.class);
    }

    // ─── token version mismatch ───────────────────────────────────

    @Test
    void refresh_shouldThrow_whenTokenVersionMismatch() {
        User user = activeUser(); // tokenVersion = 0
        setupValidRefreshToken("refresh-token", "john@example.com");
        when(jwtService.extractTokenVersion("refresh-token")).thenReturn(99); // distinto
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> refreshTokenService.refresh("refresh-token"))
                .isInstanceOf(InvalidTokenException.class);
    }

    // ─── helpers ──────────────────────────────────────────────────

    /**
     * Stubea solo hasta isTokenValid inclusive.
     * extractTokenVersion se agrega por separado en cada test que lo necesita,
     * evitando UnnecessaryStubbingException.
     */
    private void setupValidRefreshToken(String token, String email) {
        when(jwtService.extractJti(token)).thenReturn("jti-123");
        when(tokenBlacklistService.isRevoked("jti-123")).thenReturn(false);
        when(jwtService.isRefreshToken(token)).thenReturn(true);
        when(jwtService.extractEmail(token)).thenReturn(email);
        when(jwtService.isTokenValid(token, email)).thenReturn(true);
    }

    private User activeUser() {
        return User.builder()
                .id(1).email("john@example.com").password("encoded")
                .role(Role.USER).blocked(false).tokenVersion(0).build();
    }

    private User blockedUser() {
        return User.builder()
                .id(2).email("blocked@example.com").password("encoded")
                .role(Role.USER).blocked(true).tokenVersion(0).build();
    }
}