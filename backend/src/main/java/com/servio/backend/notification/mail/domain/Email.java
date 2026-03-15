package com.servio.backend.notification.mail.domain;

import lombok.Getter;

import java.util.List;

@Getter
public class Email {

    private final String from;
    private final List<String> to;
    private final String subject;
    private final String body;
    private final ContentType contentType;
    private final List<String> cc;
    private final List<String> bcc;

    private Email(Builder builder) {
        this.from = builder.from;
        this.to = builder.to;
        this.subject = builder.subject;
        this.body = builder.body;
        this.contentType = builder.contentType;
        this.cc = builder.cc != null ? builder.cc : List.of();
        this.bcc = builder.bcc != null ? builder.bcc : List.of();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String from;
        private List<String> to;
        private String subject;
        private String body;
        private ContentType contentType = ContentType.TEXT;
        private List<String> cc;
        private List<String> bcc;

        public Builder from(String from) { this.from = from; return this; }
        public Builder to(List<String> to) { this.to = to; return this; }
        public Builder to(String... to) { this.to = List.of(to); return this; }
        public Builder subject(String subject) { this.subject = subject; return this; }
        public Builder body(String body) { this.body = body; return this; }
        public Builder contentType(ContentType contentType) { this.contentType = contentType; return this; }
        public Builder cc(List<String> cc) { this.cc = cc; return this; }
        public Builder bcc(List<String> bcc) { this.bcc = bcc; return this; }

        public Email build() {
            if (from == null || from.isBlank()) throw new IllegalArgumentException("El remitente es obligatorio");
            if (to == null || to.isEmpty()) throw new IllegalArgumentException("Al menos un destinatario es obligatorio");
            if (subject == null || subject.isBlank()) throw new IllegalArgumentException("El asunto es obligatorio");
            if (body == null || body.isBlank()) throw new IllegalArgumentException("El cuerpo del correo es obligatorio");
            return new Email(this);
        }
    }
}