package com.servio.backend.identity.service.password;

import com.servio.backend.identity.dto.request.ResetPasswordRequest;
import com.servio.backend.identity.exception.InvalidTokenException;
import com.servio.backend.identity.exception.TokenExpiredException;
import com.servio.backend.identity.model.PasswordResetToken;
import com.servio.backend.identity.model.User;
import com.servio.backend.identity.repository.PasswordResetTokenRepository;
import com.servio.backend.identity.repository.UserRepository;
import com.servio.backend.mail.model.Email;
import com.servio.backend.mail.service.IEmailService;
import com.servio.backend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResetPasswordService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final IEmailService emailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${mail.default-sender}")
    private String defaultSender;

    @Value("${identity.password-reset.expiration-minutes:30}")
    private int expirationMinutes;

    public void requestReset(String email) {

        userRepository.findByEmail(email).ifPresentOrElse(user -> {
            String token = UUID.randomUUID().toString();

            tokenRepository.save(PasswordResetToken.builder()
                    .token(token)
                    .user(user)
                    .expiryDate(LocalDateTime.now().plusMinutes(expirationMinutes))
                    .build());

            sendResetEmail(user, token);

        }, () -> {
            log.warn("Password reset requested for non-existing email: {}", email);
        });
    }

    public void reset(ResetPasswordRequest request) {
        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(InvalidTokenException::new);

        if (resetToken.isExpired()) {
            throw new TokenExpiredException();
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        tokenRepository.deleteByToken(request.getToken());

        log.info("Password reset for userId={}", user.getId());
    }

    private void sendResetEmail(User user, String token) {
        try {
            emailService.send(Email.builder()
                    .from(defaultSender)
                    .to(Collections.singletonList(user.getEmail()))
                    .subject("Password recovery")
                    .template("reset_password")
                    .variables(Map.of(
                            "name", user.getFirstname(),
                            "resetUrl", frontendUrl + "/reset-password?token=" + token
                    ))
                    .build());
        } catch (Exception e) {
            log.warn("Could not send reset email to {}: {}", user.getEmail(), e.getMessage());
        }
    }
}