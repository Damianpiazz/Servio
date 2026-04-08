package com.servio.backend.mail.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class Email {

    private final String from;
    private final List<String> to;
    private final String subject;
    private final String template;
    private final Map<String, Object> variables;

    @Builder.Default
    private final List<String> cc = List.of();

    @Builder.Default
    private final List<String> bcc = List.of();
}