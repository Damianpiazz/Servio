package com.servio.backend.identity.application.port.in.usecase;

import com.servio.backend.identity.application.port.in.command.LoginCommand;
import com.servio.backend.identity.domain.Token;

public interface LoginUseCase {
    Token login(LoginCommand command);
}