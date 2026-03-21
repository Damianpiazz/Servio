package com.servio.backend.identity.application.service;

import com.servio.backend.identity.application.port.in.command.RegisterCommand;
import com.servio.backend.identity.application.port.out.TokenProviderPort;
import com.servio.backend.identity.application.port.out.UserRepositoryPort;
import com.servio.backend.identity.domain.Role;
import com.servio.backend.identity.domain.Token;
import com.servio.backend.identity.domain.User;
import com.servio.backend.identity.domain.exception.EmailAlreadyExistsException;
import com.servio.backend.notification.mail.application.port.in.SendEmailUseCase;
import com.servio.backend.shared.mail.TemplateRenderer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterServiceTest {

    @Mock private UserRepositoryPort userRepositoryPort;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TokenProviderPort tokenProviderPort;
    @Mock private SendEmailUseCase sendEmailUseCase;
    @Mock private TemplateRenderer templateRenderer;

    @InjectMocks
    private RegisterService registerService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(registerService, "defaultSender", "no-reply@servio.com");
    }

    @Test
    void debeRegistrarUsuarioCorrectamente() {
        when(userRepositoryPort.existsByEmail("juan@test.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded_password");
        when(userRepositoryPort.save(any())).thenAnswer(i -> i.getArgument(0));
        when(tokenProviderPort.generateAccessToken(any(), any())).thenReturn("access.token");
        when(tokenProviderPort.generateRefreshToken(any())).thenReturn("refresh.token");
        when(templateRenderer.render(any(), any())).thenReturn("<html>Bienvenido</html>");

        Token token = registerService.register(buildCommand());

        assertThat(token.getAccessToken()).isEqualTo("access.token");
        assertThat(token.getRefreshToken()).isEqualTo("refresh.token");
        verify(userRepositoryPort, times(1)).save(any());
    }

    @Test
    void debeFallarSiEmailYaExiste() {
        when(userRepositoryPort.existsByEmail("juan@test.com")).thenReturn(true);

        assertThatThrownBy(() -> registerService.register(buildCommand()))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("juan@test.com");
    }

    @Test
    void debeAsignarRoleUserPorDefecto() {
        when(userRepositoryPort.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(templateRenderer.render(any(), any())).thenReturn("<html></html>");
        when(userRepositoryPort.save(any())).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertThat(user.getRole()).isEqualTo(Role.USER);
            return user;
        });
        when(tokenProviderPort.generateAccessToken(any(), any())).thenReturn("access.token");
        when(tokenProviderPort.generateRefreshToken(any())).thenReturn("refresh.token");

        registerService.register(new RegisterCommand(
                "Juan", "Perez", "juan@test.com", "Password1", null
        ));
    }

    @Test
    void debeEnviarEmailDeBienvenida() {
        when(userRepositoryPort.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepositoryPort.save(any())).thenAnswer(i -> i.getArgument(0));
        when(tokenProviderPort.generateAccessToken(any(), any())).thenReturn("access.token");
        when(tokenProviderPort.generateRefreshToken(any())).thenReturn("refresh.token");
        when(templateRenderer.render(any(), any())).thenReturn("<html>Bienvenido</html>");

        registerService.register(buildCommand());

        verify(sendEmailUseCase, times(1)).send(any());
    }

    @Test
    void debeContinuarSiElEmailFalla() {
        when(userRepositoryPort.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepositoryPort.save(any())).thenAnswer(i -> i.getArgument(0));
        when(tokenProviderPort.generateAccessToken(any(), any())).thenReturn("access.token");
        when(tokenProviderPort.generateRefreshToken(any())).thenReturn("refresh.token");
        when(templateRenderer.render(any(), any())).thenReturn("<html></html>");
        doThrow(RuntimeException.class).when(sendEmailUseCase).send(any());

        assertThatNoException().isThrownBy(() -> registerService.register(buildCommand()));
    }

    private RegisterCommand buildCommand() {
        return new RegisterCommand(
                "Juan", "Perez", "juan@test.com", "Password1", Role.USER
        );
    }
}