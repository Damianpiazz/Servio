package com.servio.backend.identity.application.port.in.usecase;

import com.servio.backend.identity.application.port.in.command.ChangePasswordCommand;
import com.servio.backend.identity.domain.User;

public interface ChangePasswordUseCase {
    void changePassword(ChangePasswordCommand command, User user);
}