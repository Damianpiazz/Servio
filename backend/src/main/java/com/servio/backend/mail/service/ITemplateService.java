package com.servio.backend.mail.service;

import java.util.Map;

public interface ITemplateService {
    String render(String template, Map<String, Object> variables);
}