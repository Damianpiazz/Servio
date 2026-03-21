package com.servio.backend.identity.infrastructure.persistence.adapter;

import com.servio.backend.identity.application.port.out.UserLogoRepositoryPort;
import com.servio.backend.identity.domain.UserLogo;
import com.servio.backend.identity.infrastructure.persistence.entity.UserEntity;
import com.servio.backend.identity.infrastructure.persistence.entity.UserLogoEntity;
import com.servio.backend.identity.infrastructure.persistence.repository.UserJpaRepository;
import com.servio.backend.identity.infrastructure.persistence.repository.UserLogoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserLogoRepositoryAdapter implements UserLogoRepositoryPort {

    private final UserLogoJpaRepository jpaRepository;
    private final UserJpaRepository userJpaRepository;

    @Override
    public UserLogo save(UserLogo logo) {
        UserEntity userEntity = userJpaRepository.findById(logo.getUserId())
                .orElseThrow();
        return toDomain(jpaRepository.save(toEntity(logo, userEntity)));
    }

    @Override
    public Optional<UserLogo> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<UserLogo> findActiveByUserId(Integer userId) {
        return jpaRepository.findByUserIdAndActiveTrue(userId).map(this::toDomain);
    }

    @Override
    public List<UserLogo> findAllByUserId(Integer userId) {
        return jpaRepository.findAllByUserIdOrderByUploadedAtDesc(userId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deactivateAllByUserId(Integer userId) {
        jpaRepository.deactivateAllByUserId(userId);
    }

    @Override
    public void delete(UserLogo logo) {
        jpaRepository.deleteById(logo.getId());
    }

    private UserLogoEntity toEntity(UserLogo logo, UserEntity userEntity) {
        return UserLogoEntity.builder()
                .id(logo.getId())
                .user(userEntity)
                .url(logo.getUrl())
                .fullPath(logo.getFullPath())
                .active(logo.isActive())
                .uploadedAt(logo.getUploadedAt())
                .build();
    }

    private UserLogo toDomain(UserLogoEntity entity) {
        return UserLogo.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .url(entity.getUrl())
                .fullPath(entity.getFullPath())
                .active(entity.isActive())
                .uploadedAt(entity.getUploadedAt())
                .build();
    }
}