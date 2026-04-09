package com.servio.backend.mail.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThymeleafTemplateServiceTest {

    @Mock
    private ITemplateEngine templateEngine;

    private ThymeleafTemplateService service;

    @BeforeEach
    void setUp() {
        service = new ThymeleafTemplateService(templateEngine);
    }

    @Test
    void render_shouldReturnProcessedTemplate() {
        when(templateEngine.process(eq("register"), any(Context.class)))
                .thenReturn("<h1>Welcome John</h1>");

        String result = service.render("register", Map.of("name", "John"));

        assertThat(result).isEqualTo("<h1>Welcome John</h1>");
    }

    @Test
    void render_shouldPassVariablesToContext() {
        when(templateEngine.process(eq("reset_password"), any(Context.class)))
                .thenReturn("<a href='http://x'>Reset</a>");

        service.render("reset_password", Map.of("name", "Ana", "resetUrl", "http://x"));

        // verify template engine was called once with the right template name
        verify(templateEngine, times(1)).process(eq("reset_password"), any(Context.class));
    }

    @Test
    void render_shouldHandleEmptyVariables() {
        when(templateEngine.process(eq("simple"), any(Context.class)))
                .thenReturn("<p>Hello</p>");

        String result = service.render("simple", Map.of());

        assertThat(result).isEqualTo("<p>Hello</p>");
        verify(templateEngine).process(eq("simple"), any(Context.class));
    }
}