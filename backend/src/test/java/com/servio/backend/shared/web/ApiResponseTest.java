package com.servio.backend.shared.web;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class ApiResponseTest {

    @Test
    void okDebeRetornarStatus200SinError() {
        ApiResponse<String> response = ApiResponse.ok("data");

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getMessage()).isEqualTo("ok");
        assertThat(response.getData()).isEqualTo("data");
        assertThat(response.getError()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void okConMensajePersonalizadoDebeRetornarMensaje() {
        ApiResponse<String> response = ApiResponse.ok("mensaje custom", "data");

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getMessage()).isEqualTo("mensaje custom");
        assertThat(response.getError()).isNull();
    }

    @Test
    void createdDebeRetornarStatus201SinError() {
        ApiResponse<String> response = ApiResponse.created("data");

        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(response.getMessage()).isEqualTo("creado");
        assertThat(response.getError()).isNull();
    }

    @Test
    void noContentDebeRetornarStatus204SinError() {
        ApiResponse<Void> response = ApiResponse.noContent();

        assertThat(response.getStatus()).isEqualTo(204);
        assertThat(response.getError()).isNull();
        assertThat(response.getData()).isNull();
    }

    @Test
    void errorDebeRetornarStatusYError() {
        ApiResponse<Void> response = ApiResponse.error(HttpStatus.NOT_FOUND, "Usuario no encontrado");

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getError()).isEqualTo("Not Found");
        assertThat(response.getMessage()).isEqualTo("Usuario no encontrado");
        assertThat(response.getData()).isNull();
    }

    @Test
    void errorConDataDebeRetornarStatusErrorYData() {
        Map<String, String> errors = Map.of("email", "El email no es válido");
        ApiResponse<Map<String, String>> response = ApiResponse.error(HttpStatus.BAD_REQUEST, "Error de validación", errors);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getError()).isEqualTo("Bad Request");
        assertThat(response.getData()).containsKey("email");
    }
}