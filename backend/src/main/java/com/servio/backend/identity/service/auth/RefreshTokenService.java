package com.servio.backend.identity.service.auth;

import com.servio.backend.identity.dto.response.AuthResponse;
import com.servio.backend.identity.exception.AccountBlockedException;
import com.servio.backend.identity.exception.InvalidTokenException;
import com.servio.backend.identity.exception.TokenExpiredException;
import com.servio.backend.identity.model.User;
import com.servio.backend.identity.repository.UserRepository;
import com.servio.backend.identity.service.blacklist.TokenBlacklistService;
import com.servio.backend.identity.service.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserRepository userRepository;

    public AuthResponse refresh(String refreshToken) {

        String jti = jwtService.extractJti(refreshToken);

        if (jti == null || tokenBlacklistService.isRevoked(jti)) {
            throw new InvalidTokenException();
        }

        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new InvalidTokenException();
        }

        String email = jwtService.extractEmail(refreshToken);

        if (!jwtService.isTokenValid(refreshToken, email)) {
            throw new TokenExpiredException();
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidTokenException::new);

        if (user.isBlocked()) {
            throw new AccountBlockedException();
        }

        Integer tokenVersion = jwtService.extractTokenVersion(refreshToken);
        if (tokenVersion == null || !tokenVersion.equals(user.getTokenVersion())) {
            throw new InvalidTokenException();
        }

        tokenBlacklistService.revoke(
                jti,
                jwtService.getRefreshTokenTtlSeconds()
        );

        return new AuthResponse(
                jwtService.generateAccessToken(user.getEmail(), user.getRole().name(), user.getTokenVersion()),
                jwtService.generateRefreshToken(user.getEmail(), user.getTokenVersion())
        );
    }
}