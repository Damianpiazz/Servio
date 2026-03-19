package com.servio.backend.identity.application.service;

import com.servio.backend.identity.application.port.in.command.LoginCommand;
import com.servio.backend.identity.application.port.in.usecase.LoginUseCase;
import com.servio.backend.identity.application.port.out.TokenProviderPort;
import com.servio.backend.identity.application.port.out.UserRepositoryPort;
import com.servio.backend.identity.domain.Token;
import com.servio.backend.identity.domain.User;
import com.servio.backend.identity.domain.exception.AccountBlockedException;
import com.servio.backend.identity.domain.exception.InvalidCredentialsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {

    private final AuthenticationManager authenticationManager;
    private final UserRepositoryPort userRepositoryPort;
    private final TokenProviderPort tokenProviderPort;

    @Override
    public Token login(LoginCommand command) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(command.email(), command.password())
            );
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException();
        }

        User user = userRepositoryPort.findByEmail(command.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (user.isBlocked()) {
            throw new AccountBlockedException();
        }

        return Token.builder()
                .accessToken(tokenProviderPort.generateAccessToken(user.getEmail(), user.getRole().name()))
                .refreshToken(tokenProviderPort.generateRefreshToken(user.getEmail()))
                .build();
    }
}