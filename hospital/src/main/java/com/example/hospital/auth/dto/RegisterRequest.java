package com.example.hospital.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos para registrar un usuario. `rol` es el nombre del rol (ADMIN, MEDICO, ENFERMERO,
 * RECEPCIONISTA, FARMACEUTICO, CAJERO). Es obligatorio: no hay rol por defecto.
 * Política de contraseña: mínimo 8 caracteres (máximo 100).
 */
public record RegisterRequest(
    @NotBlank(message = "El usuario es obligatorio")
    @Size(max = 50, message = "El usuario no puede superar los 50 caracteres") String username,
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres") String password,
    @NotBlank(message = "El rol es obligatorio") String rol
) {}
