package com.example.hospital.usuarios.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;

import java.time.LocalDateTime;

/**
 * Usuario del sistema. `password` es SOLO de entrada (WRITE_ONLY): nunca aparece en las respuestas.
 * Al crear (grupo {@link Crear}) son obligatorios el usuario, la contraseña y el rol; al actualizar solo se modifica lo que se envía.
 */
public record UsuariosDTO(
    Long idUsuario,
    @NotBlank(message = "El nombre de usuario es obligatorio", groups = Crear.class)
    @Size(max = 50, message = "El nombre de usuario no puede superar los 50 caracteres", groups = {Default.class, Crear.class})
    String nombreUsuario,
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Schema(description = "Contraseña (solo de entrada; mínimo 8 caracteres). Se guarda cifrada y nunca se devuelve.", accessMode = Schema.AccessMode.WRITE_ONLY)
    @NotBlank(message = "La contraseña es obligatoria", groups = Crear.class)
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres", groups = {Default.class, Crear.class})
    String password,
    Boolean estado,
    LocalDateTime ultimoAcceso,
    @NotNull(message = "El rol es obligatorio", groups = Crear.class)
    Long idRol
) {
    /** Grupo de validación de la creación. */
    public interface Crear {}

    public static UsuariosDTO sinPassword(Long idUsuario, String nombreUsuario, Boolean estado, LocalDateTime ultimoAcceso, Long idRol) {
        return new UsuariosDTO(idUsuario, nombreUsuario, null, estado, ultimoAcceso, idRol);
    }
}
