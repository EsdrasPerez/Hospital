package com.example.hospital.estadohabitacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EstadoHabitacionDTO(
    Long idEstadoHabitacion,
    @Size(max = 30, message = "El nombre no puede superar los 30 caracteres")
    String nombre,
    @NotBlank(message = "La descripción del estado es obligatoria")
    @Size(max = 200, message = "La descripción no puede superar los 200 caracteres")
    String descripcion,
    Boolean activo
) {}
