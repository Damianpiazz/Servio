package com.servio.backend.notification.mail.infrastructure.template;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class TemplateRendererTest {

    private TemplateRenderer templateRenderer;

    @BeforeEach
    void setUp() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding("UTF-8");

        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);

        templateRenderer = new TemplateRenderer(engine);
    }

    @Test
    void debeRenderizarTemplateConMultiplesParametros() {
        String html = templateRenderer.render("test", Map.of(
                "name", "María",
                "orderId", "ORD-123",
                "total", 4500.00
        ));

        assertThat(html).contains("María");
        assertThat(html).contains("ORD-123");
        assertThat(html).contains("4500");
    }

    @Test
    void debeFallarSiElTemplateNoExiste() {
        assertThatThrownBy(() ->
                templateRenderer.render("template_inexistente", Map.of())
        ).isInstanceOf(Exception.class);
    }
}