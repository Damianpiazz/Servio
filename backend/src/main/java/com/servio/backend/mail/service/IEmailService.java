package com.servio.backend.mail.service;

import com.servio.backend.mail.model.Email;

public interface IEmailService {
    void send(Email email);
}