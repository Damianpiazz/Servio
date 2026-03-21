package com.servio.backend.identity.application.service;

import com.servio.backend.identity.application.port.in.usecase.GetUserLogosUseCase;
import com.servio.backend.identity.application.port.out.UserLogoRepositoryPort;
import com.servio.backend.identity.domain.UserLogo;
import com.servio.backend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetUserLogosService implements GetUserLogosUseCase {

    private final UserLogoRepositoryPort userLogoRepositoryPort;

    @Override
    public List<UserLogo> getHistory(Integer userId) {
        return userLogoRepositoryPort.findAllByUserId(userId);
    }

    @Override
    public UserLogo getActive(Integer userId) {
        return userLogoRepositoryPort.findActiveByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Logo activo", "userId", userId));
    }
}