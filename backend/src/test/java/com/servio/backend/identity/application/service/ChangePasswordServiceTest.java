package com.servio.backend.identity.application.service;

import com.servio.backend.identity.application.port.in.command.ChangePasswordCommand;
import com.servio.backend.identity.application.port.out.UserRepositoryPort;
import com.servio.backend.identity.domain.Role;
import com.servio.backend.identity.domain.User;
import com.servio.backend.notification.mail.application.port.in.SendEmailUseCase;
import com.servio.backend.notification.mail.infrastructure.template.TemplateRenderer;
import com.servio.backend.shared.exception.InvalidArgumentException;
import com.servio.backend.shared.exception.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangePasswordServiceTest {

    @Mock private UserRepositoryPort userRepositoryPort;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private SendEmailUseCase sendEmailUseCase;
    @Mock private TemplateRenderer templateRenderer;

    @InjectMocks
    private ChangePasswordService changePasswordService;

    @Test
    void debeCambiarContrasenaCorrectamente() throws Exception {
        User user = buildUser();
        when(passwordEncoder.matches("Password1", "encoded_password")).thenReturn(true);
        when(passwordEncoder.encode("Password2")).thenReturn("new_encoded_password");
        when(templateRenderer.render(any(), any())).thenReturn("<html></html>");

        changePasswordService.changePassword(
                new ChangePasswordCommand("Password1", "Password2", "Password2"),
                user
        );

        verify(userRepositoryPort, times(1)).save(user);
        assertThat(user.getPassword()).isEqualTo("new_encoded_password");
    }

    @Test
    void debeFallarSiContrasenaActualIncorrecta() {
        User user = buildUser();
        when(passwordEncoder.matches("wrong", "encoded_password")).thenReturn(false);

        assertThatThrownBy(() -> changePasswordService.changePassword(
                new ChangePasswordCommand("wrong", "Password2", "Password2"),
                user
        )).isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void debeFallarSiContrasenasNoCoinciden() {
        User user = buildUser();
        when(passwordEncoder.matches("Password1", "encoded_password")).thenReturn(true);

        assertThatThrownBy(() -> changePasswordService.changePassword(
                new ChangePasswordCommand("Password1", "Password2", "Password3"),
                user
        )).isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("no coinciden");
    }

    private User buildUser() {
        return User.builder()
                .id(1).firstname("Juan").lastname("Perez")
                .email("juan@test.com").password("encoded_password")
                .role(Role.USER).blocked(false)
                .build();
    }
}