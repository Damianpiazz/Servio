package com.servio.backend.identity.service.auth;

import com.servio.backend.identity.dto.request.RegisterRequest;
import com.servio.backend.identity.dto.response.AuthResponse;
import com.servio.backend.identity.exception.EmailAlreadyExistsException;
import com.servio.backend.identity.model.Role;
import com.servio.backend.identity.model.User;
import com.servio.backend.identity.repository.UserRepository;
import com.servio.backend.identity.service.jwt.JwtService;
import com.servio.backend.mail.service.IEmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private IEmailService emailService;

    @InjectMocks private RegisterService registerService;

    // ─── happy path ───────────────────────────────────────────────

    @Test
    void register_shouldSaveUserAndReturnTokens() {
        RegisterRequest req = validRequest();
        when(userRepository.existsByEmail(req.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded-pass");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            ReflectionTestUtils.setField(u, "id", 1);
            return u;
        });
        when(jwtService.generateAccessToken(any(), any(), anyInt())).thenReturn("access");
        when(jwtService.generateRefreshToken(any(), anyInt())).thenReturn("refresh");

        AuthResponse response = registerService.register(req);

        assertThat(response.getAccessToken()).isEqualTo("access");
        assertThat(response.getRefreshToken()).isEqualTo("refresh");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldAssignRoleUser() {
        RegisterRequest req = validRequest();
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(jwtService.generateAccessToken(any(), any(), anyInt())).thenReturn("a");
        when(jwtService.generateRefreshToken(any(), anyInt())).thenReturn("r");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        registerService.register(req);

        assertThat(captor.getValue().getRole()).isEqualTo(Role.USER);
        assertThat(captor.getValue().isBlocked()).isFalse();
    }

    @Test
    void register_shouldEncodePassword() {
        RegisterRequest req = validRequest();
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode("Password1")).thenReturn("bcrypt-hash");
        when(jwtService.generateAccessToken(any(), any(), anyInt())).thenReturn("a");
        when(jwtService.generateRefreshToken(any(), anyInt())).thenReturn("r");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        registerService.register(req);

        assertThat(captor.getValue().getPassword()).isEqualTo("bcrypt-hash");
    }

    @Test
    void register_shouldSendWelcomeEmail() {
        RegisterRequest req = validRequest();
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateAccessToken(any(), any(), anyInt())).thenReturn("a");
        when(jwtService.generateRefreshToken(any(), anyInt())).thenReturn("r");

        registerService.register(req);

        verify(emailService).send(any());
    }

    // ─── duplicate email ──────────────────────────────────────────

    @Test
    void register_shouldThrow_whenEmailAlreadyExists() {
        RegisterRequest req = validRequest();
        when(userRepository.existsByEmail(req.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> registerService.register(req))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    // ─── email failure doesn't break registration ─────────────────

    @Test
    void register_shouldSucceed_evenIfEmailServiceThrows() {
        RegisterRequest req = validRequest();
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateAccessToken(any(), any(), anyInt())).thenReturn("a");
        when(jwtService.generateRefreshToken(any(), anyInt())).thenReturn("r");
        doThrow(new RuntimeException("SMTP down")).when(emailService).send(any());

        assertThatNoException().isThrownBy(() -> registerService.register(req));
    }

    // ─── helper ───────────────────────────────────────────────────

    private RegisterRequest validRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setFirstname("John");
        req.setLastname("Doe");
        req.setEmail("john@example.com");
        req.setPassword("Password1");
        return req;
    }
}