package com.servio.backend.identity.service.auth;

import com.servio.backend.identity.dto.request.LoginRequest;
import com.servio.backend.identity.dto.response.AuthResponse;
import com.servio.backend.identity.exception.AccountBlockedException;
import com.servio.backend.identity.exception.InvalidCredentialsException;
import com.servio.backend.identity.model.Role;
import com.servio.backend.identity.model.User;
import com.servio.backend.identity.repository.UserRepository;
import com.servio.backend.identity.service.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;

    @InjectMocks private LoginService loginService;

    // ─── happy path ───────────────────────────────────────────────

    @Test
    void login_shouldReturnTokens_whenCredentialsAreValid() {
        User user = activeUser();
        LoginRequest req = new LoginRequest();
        req.setEmail("john@example.com");
        req.setPassword("Password1");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(any(), any(), anyInt())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(), anyInt())).thenReturn("refresh-token");

        AuthResponse response = loginService.login(req);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
    }

    // ─── bad credentials ──────────────────────────────────────────

    @Test
    void login_shouldThrow_whenCredentialsAreInvalid() {
        LoginRequest req = new LoginRequest();
        req.setEmail("john@example.com");
        req.setPassword("wrong");

        doThrow(new BadCredentialsException("bad"))
                .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThatThrownBy(() -> loginService.login(req))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    // ─── blocked account ──────────────────────────────────────────

    @Test
    void login_shouldThrow_whenAccountIsBlocked() {
        User user = blockedUser();
        LoginRequest req = new LoginRequest();
        req.setEmail("blocked@example.com");
        req.setPassword("Password1");

        when(userRepository.findByEmail("blocked@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> loginService.login(req))
                .isInstanceOf(AccountBlockedException.class);
    }

    // ─── helpers ──────────────────────────────────────────────────

    private User activeUser() {
        return User.builder()
                .id(1)
                .email("john@example.com")
                .password("encoded")
                .role(Role.USER)
                .blocked(false)
                .tokenVersion(0)
                .build();
    }

    private User blockedUser() {
        return User.builder()
                .id(2)
                .email("blocked@example.com")
                .password("encoded")
                .role(Role.USER)
                .blocked(true)
                .tokenVersion(0)
                .build();
    }
}