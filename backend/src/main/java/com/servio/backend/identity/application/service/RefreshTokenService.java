package com.servio.backend.identity.application.service;

import com.servio.backend.identity.application.port.in.usecase.RefreshTokenUseCase;
import com.servio.backend.identity.application.port.out.TokenBlacklistPort;
import com.servio.backend.identity.application.port.out.TokenProviderPort;
import com.servio.backend.identity.application.port.out.UserRepositoryPort;
import com.servio.backend.identity.domain.Token;
import com.servio.backend.identity.domain.User;
import com.servio.backend.identity.domain.exception.AccountBlockedException;
import com.servio.backend.identity.domain.exception.InvalidTokenException;
import com.servio.backend.identity.domain.exception.TokenExpiredException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService implements RefreshTokenUseCase {

    private final TokenProviderPort tokenProviderPort;
    private final TokenBlacklistPort tokenBlacklistPort;
    private final UserRepositoryPort userRepositoryPort;

    @Override
    public Token refresh(String refreshToken) {
        if (tokenBlacklistPort.isRevoked(refreshToken)) {
            throw new InvalidTokenException();
        }

        String email = tokenProviderPort.extractEmail(refreshToken);
        if (email == null) {
            throw new InvalidTokenException();
        }

        if (!tokenProviderPort.isTokenValid(refreshToken, email)) {
            throw new TokenExpiredException();
        }

        User user = userRepositoryPort.findByEmail(email)
                .orElseThrow(InvalidTokenException::new);

        if (user.isBlocked()) {
            throw new AccountBlockedException();
        }

        return Token.builder()
                .accessToken(tokenProviderPort.generateAccessToken(user.getEmail(), user.getRole().name()))
                .refreshToken(refreshToken)
                .build();
    }
}