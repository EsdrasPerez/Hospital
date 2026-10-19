package com.example.hospital.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Datos para registrar un usuario. `rol` es el nombre del rol (ADMIN, MEDICO, ENFERMERO,
 * RECEPCIONISTA, FARMACEUTICO, CAJERO). Es obligatorio: no hay rol por defecto.
 */
public record RegisterRequest(
    @NotBlank(message = "El usuario es obligatorio") String username,
    @NotBlank(message = "La contraseña es obligatoria") String password,
    @NotBlank(message = "El rol es obligatorio") String rol
) {}
