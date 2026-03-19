package com.servio.backend.identity.infrastructure.web.dto;

import com.servio.backend.identity.domain.Role;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String firstname;

    @NotBlank(message = "El apellido es obligatorio")
    private String lastname;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no es válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Pattern(regexp = ".*[A-Z].*", message = "Debe incluir al menos una mayúscula")
    @Pattern(regexp = ".*[a-z].*", message = "Debe incluir al menos una minúscula")
    @Pattern(regexp = ".*[0-9].*", message = "Debe incluir al menos un número")
    private String password;

    private Role role;
}