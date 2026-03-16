package com.servio.backend.shared.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.*;

class AppExceptionTest {

    @Test
    void debeGuardarMensajeYStatus() {
        AppException ex = new AppException("mensaje", HttpStatus.BAD_REQUEST);

        assertThat(ex.getMessage()).isEqualTo("mensaje");
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void resourceNotFoundDebeRetornar404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Usuario");

        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getMessage()).contains("Usuario");
    }

    @Test
    void resourceNotFoundConFieldDebeIncluirDetalle() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Usuario", "id", 42);

        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getMessage()).contains("Usuario").contains("id").contains("42");
    }

    @Test
    void forbiddenDebeRetornar403() {
        ForbiddenException ex = new ForbiddenException();

        assertThat(ex.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(ex.getMessage()).isNotBlank();
    }

    @Test
    void forbiddenConMensajePersonalizadoDebeUsarloMensaje() {
        ForbiddenException ex = new ForbiddenException("No podés modificar esto");

        assertThat(ex.getMessage()).isEqualTo("No podés modificar esto");
    }

    @Test
    void invalidArgumentDebeRetornar400() {
        InvalidArgumentException ex = new InvalidArgumentException("El archivo es muy grande");

        assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getMessage()).isEqualTo("El archivo es muy grande");
    }

    @Test
    void conflictDebeRetornar409() {
        ConflictException ex = new ConflictException("Ya existe");

        assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(ex.getMessage()).isEqualTo("Ya existe");
    }

    @Test
    void unauthorizedDebeRetornar401() {
        UnauthorizedException ex = new UnauthorizedException();

        assertThat(ex.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(ex.getMessage()).isNotBlank();
    }

    @Test
    void unauthorizedConMensajeDebeUsarloMensaje() {
        UnauthorizedException ex = new UnauthorizedException("Token inválido");

        assertThat(ex.getMessage()).isEqualTo("Token inválido");
    }

    @Test
    void businessExceptionDebeRetornar422() {
        BusinessException ex = new BusinessException("No podés publicar más de 5 servicios");

        assertThat(ex.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(ex.getMessage()).isEqualTo("No podés publicar más de 5 servicios");
    }
}