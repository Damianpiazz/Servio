package com.servio.backend.identity.application.port.in.usecase;

import com.servio.backend.identity.application.port.in.command.ResetPasswordCommand;

public interface ResetPasswordUseCase {
    void requestReset(String email);
    void reset(ResetPasswordCommand command);
}