package com.servio.backend.identity.service.password;

import com.servio.backend.identity.dto.request.ChangePasswordRequest;
import com.servio.backend.identity.model.User;
import com.servio.backend.identity.repository.UserRepository;
import com.servio.backend.mail.model.Email;
import com.servio.backend.mail.service.IEmailService;
import com.servio.backend.shared.exception.AppException;
import com.servio.backend.shared.exception.InvalidArgumentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChangePasswordService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final IEmailService emailService;

    @Value("${mail.default-sender}")
    private String defaultSender;

    public void changePassword(ChangePasswordRequest request, User user) {
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new AppException("Current password is incorrect", HttpStatus.UNAUTHORIZED) {};
        }

        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            throw new InvalidArgumentException("Passwords do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
        log.info("Password changed for userId={}", user.getId());
        sendConfirmationEmail(user);
    }

    private void sendConfirmationEmail(User user) {
        try {
            emailService.send(Email.builder()
                    .from(defaultSender)
                    .to(Collections.singletonList(user.getEmail()))
                    .subject("Your password was changed")
                    .template("password_changed")
                    .variables(Map.of("name", user.getFirstname()))
                    .build());
        } catch (Exception e) {
            log.warn("Could not send confirmation email to {}: {}", user.getEmail(), e.getMessage());
        }
    }
}