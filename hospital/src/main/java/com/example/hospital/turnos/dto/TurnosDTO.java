package com.example.hospital.turnos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TurnosDTO(
    Long idTurno,
    @NotBlank(message = "La descripción del turno es obligatoria")
    @Size(max = 30, message = "La descripción no puede superar los 30 caracteres")
    String descripcion,
    @NotNull(message = "La hora de inicio es obligatoria")
    java.time.LocalTime horaInicio,
    @NotNull(message = "La hora de fin es obligatoria")
    java.time.LocalTime horaFin,
    Boolean activo
) {}
