package com.servio.backend.shared.web;

import com.servio.backend.shared.exception.AppException;
import com.servio.backend.shared.exception.ResourceNotFoundException;
import com.servio.backend.shared.exception.ForbiddenException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Test
    void debeManejarsResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Usuario", "id", 42);

        ResponseEntity<ApiResponse<Void>> response = handler.handleAppException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getMessage()).contains("Usuario");
    }

    @Test
    void debeManejarForbiddenException() {
        ForbiddenException ex = new ForbiddenException();

        ResponseEntity<ApiResponse<Void>> response = handler.handleAppException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getStatus()).isEqualTo(403);
        assertThat(response.getBody().getError()).isEqualTo("Forbidden");
    }

    @Test
    void debeManejarErroresDeValidacion() {
        BindingResult bindingResult = mock(BindingResult.class);
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("obj", "email", "El email no es válido"),
                new FieldError("obj", "password", "Debe tener al menos 8 caracteres")
        ));

        ResponseEntity<ApiResponse<Map<String, String>>> response = handler.handleValidationErrors(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getData()).containsKeys("email", "password");
    }

    @Test
    void debeManejarExcepcionInesperada() {
        Exception ex = new RuntimeException("algo salió mal");

        ResponseEntity<ApiResponse<Void>> response = handler.handleUnexpected(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).isEqualTo("Error interno del servidor");
    }

    @Test
    void errorNoDebeExponerDetallesInternos() {
        Exception ex = new RuntimeException("detalles internos sensibles");

        ResponseEntity<ApiResponse<Void>> response = handler.handleUnexpected(ex);

        assertThat(response.getBody().getMessage()).doesNotContain("detalles internos sensibles");
    }
}