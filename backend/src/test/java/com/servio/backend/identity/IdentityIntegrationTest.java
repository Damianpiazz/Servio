package com.servio.backend.identity;

import com.servio.backend.BaseIntegrationTest;
import com.servio.backend.identity.application.port.in.command.LoginCommand;
import com.servio.backend.identity.application.port.in.command.RegisterCommand;
import com.servio.backend.identity.application.port.in.usecase.LoginUseCase;
import com.servio.backend.identity.application.port.in.usecase.LogoutUseCase;
import com.servio.backend.identity.application.port.in.usecase.RegisterUseCase;
import com.servio.backend.identity.application.port.out.TokenBlacklistPort;
import com.servio.backend.identity.domain.Role;
import com.servio.backend.identity.domain.Token;
import com.servio.backend.identity.domain.exception.EmailAlreadyExistsException;
import com.servio.backend.identity.domain.exception.InvalidCredentialsException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.*;

class IdentityIntegrationTest extends BaseIntegrationTest {

    @Autowired private RegisterUseCase registerUseCase;
    @Autowired private LoginUseCase loginUseCase;
    @Autowired private LogoutUseCase logoutUseCase;
    @Autowired private TokenBlacklistPort tokenBlacklistPort;

    @Test
    void debeRegistrarYLoginarCorrectamente() {
        Token token = registerUseCase.register(new RegisterCommand(
                "Juan", "Perez", "integration@test.com", "Password1", Role.USER
        ));
        assertThat(token.getAccessToken()).isNotBlank();
        assertThat(token.getRefreshToken()).isNotBlank();

        Token loginToken = loginUseCase.login(new LoginCommand(
                "integration@test.com", "Password1"
        ));
        assertThat(loginToken.getAccessToken()).isNotBlank();
    }

    @Test
    void debeFallarSiEmailYaRegistrado() {
        registerUseCase.register(new RegisterCommand(
                "Juan", "Perez", "duplicado@test.com", "Password1", Role.USER
        ));

        assertThatThrownBy(() -> registerUseCase.register(new RegisterCommand(
                "Juan", "Perez", "duplicado@test.com", "Password1", Role.USER
        ))).isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void debeFallarSiCredencialesInvalidas() {
        registerUseCase.register(new RegisterCommand(
                "Juan", "Perez", "credenciales@test.com", "Password1", Role.USER
        ));

        assertThatThrownBy(() -> loginUseCase.login(
                new LoginCommand("credenciales@test.com", "WrongPassword1")
        )).isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void debeRevocarTokenAlHacerLogout() {
        Token token = registerUseCase.register(new RegisterCommand(
                "Juan", "Perez", "logout@test.com", "Password1", Role.USER
        ));

        logoutUseCase.logout(token.getAccessToken());

        assertThat(tokenBlacklistPort.isRevoked(token.getAccessToken())).isTrue();
    }
}