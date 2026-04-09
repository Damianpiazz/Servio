package com.servio.backend.identity.service.password;

import com.servio.backend.identity.dto.request.ChangePasswordRequest;
import com.servio.backend.identity.model.Role;
import com.servio.backend.identity.model.User;
import com.servio.backend.identity.repository.UserRepository;
import com.servio.backend.mail.service.IEmailService;
import com.servio.backend.shared.exception.AppException;
import com.servio.backend.shared.exception.InvalidArgumentException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangePasswordServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private IEmailService emailService;

    @InjectMocks private ChangePasswordService changePasswordService;

    // ─── happy path ───────────────────────────────────────────────

    @Test
    void changePassword_shouldUpdatePassword_whenRequestIsValid() {
        User user = user("encoded-old");
        ChangePasswordRequest req = request("OldPassword1", "NewPassword1", "NewPassword1");

        when(passwordEncoder.matches("OldPassword1", "encoded-old")).thenReturn(true);
        when(passwordEncoder.encode("NewPassword1")).thenReturn("encoded-new");

        changePasswordService.changePassword(req, user);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("encoded-new");
    }

    @Test
    void changePassword_shouldIncrementTokenVersion() {
        User user = user("encoded-old");
        int originalVersion = user.getTokenVersion();
        ChangePasswordRequest req = request("OldPassword1", "NewPassword1", "NewPassword1");

        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(passwordEncoder.encode(any())).thenReturn("encoded-new");

        changePasswordService.changePassword(req, user);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getTokenVersion()).isEqualTo(originalVersion + 1);
    }

    @Test
    void changePassword_shouldSendConfirmationEmail() {
        User user = user("encoded-old");
        ChangePasswordRequest req = request("OldPassword1", "NewPassword1", "NewPassword1");

        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(passwordEncoder.encode(any())).thenReturn("encoded-new");
        when(userRepository.save(any())).thenReturn(user);

        changePasswordService.changePassword(req, user);

        verify(emailService).send(any());
    }

    // ─── wrong current password ───────────────────────────────────

    @Test
    void changePassword_shouldThrow_whenCurrentPasswordIsWrong() {
        User user = user("encoded-old");
        ChangePasswordRequest req = request("WrongPassword1", "NewPassword1", "NewPassword1");

        when(passwordEncoder.matches("WrongPassword1", "encoded-old")).thenReturn(false);

        assertThatThrownBy(() -> changePasswordService.changePassword(req, user))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("incorrect");

        verify(userRepository, never()).save(any());
    }

    // ─── passwords don't match ────────────────────────────────────

    @Test
    void changePassword_shouldThrow_whenPasswordsDoNotMatch() {
        User user = user("encoded-old");
        ChangePasswordRequest req = request("OldPassword1", "NewPassword1", "Different1");

        when(passwordEncoder.matches("OldPassword1", "encoded-old")).thenReturn(true);

        assertThatThrownBy(() -> changePasswordService.changePassword(req, user))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("match");

        verify(userRepository, never()).save(any());
    }

    // ─── email failure doesn't break the flow ─────────────────────

    @Test
    void changePassword_shouldSucceed_evenIfEmailFails() {
        User user = user("encoded-old");
        ChangePasswordRequest req = request("OldPassword1", "NewPassword1", "NewPassword1");

        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(passwordEncoder.encode(any())).thenReturn("encoded-new");
        when(userRepository.save(any())).thenReturn(user);
        doThrow(new RuntimeException("SMTP error")).when(emailService).send(any());

        assertThatNoException().isThrownBy(() -> changePasswordService.changePassword(req, user));
    }

    // ─── helpers ──────────────────────────────────────────────────

    private User user(String encodedPassword) {
        return User.builder()
                .id(1).email("john@example.com").firstname("John")
                .password(encodedPassword).role(Role.USER)
                .blocked(false).tokenVersion(3).build();
    }

    private ChangePasswordRequest request(String current, String newPwd, String confirm) {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword(current);
        req.setNewPassword(newPwd);
        req.setConfirmationPassword(confirm);
        return req;
    }
}