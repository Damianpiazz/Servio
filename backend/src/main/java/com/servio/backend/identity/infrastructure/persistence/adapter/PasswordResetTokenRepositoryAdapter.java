package com.servio.backend.identity.infrastructure.persistence.adapter;

import com.servio.backend.identity.application.port.out.PasswordResetTokenRepositoryPort;
import com.servio.backend.identity.domain.User;
import com.servio.backend.identity.infrastructure.persistence.entity.PasswordResetTokenEntity;
import com.servio.backend.identity.infrastructure.persistence.entity.UserEntity;
import com.servio.backend.identity.infrastructure.persistence.repository.PasswordResetTokenJpaRepository;
import com.servio.backend.identity.infrastructure.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PasswordResetTokenRepositoryAdapter implements PasswordResetTokenRepositoryPort {

    private final PasswordResetTokenJpaRepository jpaRepository;
    private final UserJpaRepository userJpaRepository;

    @Override
    public void save(String token, User user, LocalDateTime expiryDate) {
        UserEntity userEntity = userJpaRepository.findByEmail(user.getEmail())
                .orElseThrow();

        jpaRepository.save(PasswordResetTokenEntity.builder()
                .token(token)
                .user(userEntity)
                .expiryDate(expiryDate)
                .build());
    }

    @Override
    public Optional<PasswordResetTokenData> findByToken(String token) {
        return jpaRepository.findByToken(token).map(entity -> {
            User user = User.builder()
                    .id(entity.getUser().getId())
                    .firstname(entity.getUser().getFirstname())
                    .lastname(entity.getUser().getLastname())
                    .email(entity.getUser().getEmail())
                    .password(entity.getUser().getPassword())
                    .role(entity.getUser().getRole())
                    .blocked(entity.getUser().isBlocked())
                    .build();
            return new PasswordResetTokenData(entity.getToken(), user, entity.getExpiryDate());
        });
    }

    @Override
    public void delete(String token) {
        jpaRepository.deleteByToken(token);
    }
}