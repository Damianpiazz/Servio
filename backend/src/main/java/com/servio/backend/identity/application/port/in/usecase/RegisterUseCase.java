package com.servio.backend.identity.application.port.in.usecase;

import com.servio.backend.identity.application.port.in.command.RegisterCommand;
import com.servio.backend.identity.domain.Token;

public interface RegisterUseCase {
    Token register(RegisterCommand command);
}