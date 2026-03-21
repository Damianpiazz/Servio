package com.servio.backend.identity.infrastructure.web.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    @NotBlank(message = "La contraseña actual es obligatoria")
    private String currentPassword;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Pattern(regexp = ".*[A-Z].*", message = "Debe incluir al menos una mayúscula")
    @Pattern(regexp = ".*[a-z].*", message = "Debe incluir al menos una minúscula")
    @Pattern(regexp = ".*[0-9].*", message = "Debe incluir al menos un número")
    private String newPassword;

    @NotBlank(message = "La confirmación es obligatoria")
    private String confirmationPassword;
}