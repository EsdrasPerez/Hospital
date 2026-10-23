package com.example.hospital.asignacionenfermeros.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AsignacionEnfermerosDTO(
    Long idAsignacion,
    @NotNull(message = "El enfermero es obligatorio")
    Long idEnfermero,
    @NotNull(message = "La hospitalización es obligatoria")
    Long idHospitalizacion,
    @NotNull(message = "La fecha de asignación es obligatoria")
    LocalDate fechaAsignacion,
    @NotNull(message = "El turno es obligatorio")
    Long idTurno,
    Boolean activo
) {}
