package com.servio.backend.identity.application.service;

import com.servio.backend.identity.application.port.in.command.LoginCommand;
import com.servio.backend.identity.application.port.out.TokenProviderPort;
import com.servio.backend.identity.application.port.out.UserRepositoryPort;
import com.servio.backend.identity.domain.Role;
import com.servio.backend.identity.domain.Token;
import com.servio.backend.identity.domain.User;
import com.servio.backend.identity.domain.exception.AccountBlockedException;
import com.servio.backend.identity.domain.exception.InvalidCredentialsException;
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

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepositoryPort userRepositoryPort;
    @Mock private TokenProviderPort tokenProviderPort;

    @InjectMocks
    private LoginService loginService;

    @Test
    void debeLoginarCorrectamente() {
        User user = buildUser(false);
        when(userRepositoryPort.findByEmail("juan@test.com")).thenReturn(Optional.of(user));
        when(tokenProviderPort.generateAccessToken(any(), any())).thenReturn("access.token");
        when(tokenProviderPort.generateRefreshToken(any())).thenReturn("refresh.token");

        Token token = loginService.login(new LoginCommand("juan@test.com", "Password1"));

        assertThat(token.getAccessToken()).isEqualTo("access.token");
        assertThat(token.getRefreshToken()).isEqualTo("refresh.token");
    }

    @Test
    void debeFallarSiCredencialesInvalidas() {
        doThrow(BadCredentialsException.class)
                .when(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> loginService.login(new LoginCommand("juan@test.com", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void debeFallarSiCuentaBloqueada() {
        User user = buildUser(true);
        when(userRepositoryPort.findByEmail("juan@test.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> loginService.login(new LoginCommand("juan@test.com", "Password1")))
                .isInstanceOf(AccountBlockedException.class);
    }

    private User buildUser(boolean blocked) {
        return User.builder()
                .id(1)
                .firstname("Juan")
                .lastname("Perez")
                .email("juan@test.com")
                .password("encoded_password")
                .role(Role.USER)
                .blocked(blocked)
                .build();
    }
}