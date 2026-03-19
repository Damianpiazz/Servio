package com.servio.backend.identity.application.port.out;

import com.servio.backend.identity.domain.User;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepositoryPort {
    void save(String token, User user, LocalDateTime expiryDate);
    Optional<PasswordResetTokenData> findByToken(String token);
    void delete(String token);

    record PasswordResetTokenData(String token, User user, LocalDateTime expiryDate) {
        public boolean isExpired() {
            return expiryDate.isBefore(LocalDateTime.now());
        }
    }
}