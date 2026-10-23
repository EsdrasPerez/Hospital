package com.example.hospital.rol.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RolDTO(
    Long idRol,
    @NotBlank(message = "El nombre del rol es obligatorio")
    @Size(max = 50, message = "El nombre del rol no puede superar los 50 caracteres")
    String nombre,
    @Size(max = 200, message = "La descripción no puede superar los 200 caracteres")
    String descripcion,
    Boolean activo
) {}
