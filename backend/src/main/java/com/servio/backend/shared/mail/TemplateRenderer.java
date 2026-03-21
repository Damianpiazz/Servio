package com.servio.backend.shared.mail;

import java.util.Map;

public interface TemplateRenderer {
    String render(String template, Map<String, Object> variables);
}