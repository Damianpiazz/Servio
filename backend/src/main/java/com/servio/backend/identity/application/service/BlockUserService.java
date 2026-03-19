package com.servio.backend.identity.application.service;

import com.servio.backend.identity.application.port.in.usecase.BlockUserUseCase;
import com.servio.backend.identity.application.port.out.UserRepositoryPort;
import com.servio.backend.identity.domain.User;
import com.servio.backend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BlockUserService implements BlockUserUseCase {

    private final UserRepositoryPort userRepositoryPort;

    @Override
    public void block(Integer userId) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));
        user.setBlocked(true);
        userRepositoryPort.save(user);
    }

    @Override
    public void unblock(Integer userId) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));
        user.setBlocked(false);
        userRepositoryPort.save(user);
    }
}