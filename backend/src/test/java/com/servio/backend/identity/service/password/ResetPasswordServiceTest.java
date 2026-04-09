package com.servio.backend.identity.service.password;

import com.servio.backend.identity.dto.request.ResetPasswordRequest;
import com.servio.backend.identity.exception.InvalidTokenException;
import com.servio.backend.identity.exception.TokenExpiredException;
import com.servio.backend.identity.model.PasswordResetToken;
import com.servio.backend.identity.model.Role;
import com.servio.backend.identity.model.User;
import com.servio.backend.identity.repository.PasswordResetTokenRepository;
import com.servio.backend.identity.repository.UserRepository;
import com.servio.backend.mail.service.IEmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResetPasswordServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordResetTokenRepository tokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private IEmailService emailService;

    @InjectMocks private ResetPasswordService resetPasswordService;

    // ─── requestReset ─────────────────────────────────────────────

    @Test
    void requestReset_shouldSaveTokenAndSendEmail_whenUserExists() {
        User user = user();
        ReflectionTestUtils.setField(resetPasswordService, "frontendUrl", "http://localhost:3001");
        ReflectionTestUtils.setField(resetPasswordService, "defaultSender", "no-reply@servio.com");
        ReflectionTestUtils.setField(resetPasswordService, "expirationMinutes", 30);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        resetPasswordService.requestReset("john@example.com");

        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(emailService).send(any());
    }

    @Test
    void requestReset_shouldDoNothing_whenUserNotFound() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        resetPasswordService.requestReset("ghost@example.com");

        verify(tokenRepository, never()).save(any());
        verifyNoInteractions(emailService);
    }

    @Test
    void requestReset_shouldSucceed_evenIfEmailFails() {
        User user = user();
        ReflectionTestUtils.setField(resetPasswordService, "frontendUrl", "http://localhost:3001");
        ReflectionTestUtils.setField(resetPasswordService, "defaultSender", "no-reply@servio.com");
        ReflectionTestUtils.setField(resetPasswordService, "expirationMinutes", 30);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new RuntimeException("SMTP down")).when(emailService).send(any());

        assertThatNoException().isThrownBy(() -> resetPasswordService.requestReset("john@example.com"));
    }

    // ─── reset ────────────────────────────────────────────────────

    @Test
    void reset_shouldUpdatePassword_whenTokenIsValid() {
        User user = user();
        PasswordResetToken token = validToken(user);
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("valid-token");
        req.setNewPassword("NewPassword1");

        when(tokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("NewPassword1")).thenReturn("encoded-new");

        resetPasswordService.reset(req);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("encoded-new");
    }

    @Test
    void reset_shouldDeleteTokenAfterSuccessfulReset() {
        User user = user();
        PasswordResetToken token = validToken(user);
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("valid-token");
        req.setNewPassword("NewPassword1");

        when(tokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode(any())).thenReturn("encoded");

        resetPasswordService.reset(req);

        verify(tokenRepository).deleteByToken("valid-token");
    }

    @Test
    void reset_shouldThrow_whenTokenNotFound() {
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("nonexistent");
        req.setNewPassword("NewPassword1");

        when(tokenRepository.findByToken("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resetPasswordService.reset(req))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void reset_shouldThrow_whenTokenIsExpired() {
        User user = user();
        PasswordResetToken expiredToken = PasswordResetToken.builder()
                .token("expired-token")
                .user(user)
                .expiryDate(LocalDateTime.now().minusMinutes(1))
                .build();

        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("expired-token");
        req.setNewPassword("NewPassword1");

        when(tokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> resetPasswordService.reset(req))
                .isInstanceOf(TokenExpiredException.class);

        verify(userRepository, never()).save(any());
    }

    // ─── helpers ──────────────────────────────────────────────────

    private User user() {
        return User.builder()
                .id(1).email("john@example.com").firstname("John")
                .password("encoded-old").role(Role.USER)
                .blocked(false).tokenVersion(0).build();
    }

    private PasswordResetToken validToken(User user) {
        return PasswordResetToken.builder()
                .token("valid-token")
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(30))
                .build();
    }
}