package com.servio.backend.notification.mail.application.port.in;

import com.servio.backend.notification.mail.domain.Email;

public interface SendEmailUseCase {
    void send(Email email);
}