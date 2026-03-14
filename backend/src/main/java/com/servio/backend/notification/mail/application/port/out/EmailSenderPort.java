package com.servio.backend.notification.mail.application.port.out;

import com.servio.backend.notification.mail.domain.Email;

public interface EmailSenderPort {
    void send(Email email);
}