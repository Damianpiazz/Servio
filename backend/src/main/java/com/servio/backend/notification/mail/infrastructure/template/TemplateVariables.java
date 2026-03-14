package com.servio.backend.notification.mail.infrastructure.template;

import java.util.HashMap;
import java.util.Map;

public class TemplateVariables {

    private final Map<String, Object> variables = new HashMap<>();

    private TemplateVariables() {}

    public static TemplateVariables create() {
        return new TemplateVariables();
    }

    public TemplateVariables add(String key, Object value) {
        variables.put(key, value);
        return this;
    }

    public Map<String, Object> build() {
        return Map.copyOf(variables);
    }
}
