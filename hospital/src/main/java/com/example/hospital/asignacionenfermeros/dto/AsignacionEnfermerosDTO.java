package com.example.hospital.asignacionenfermeros.dto;

import java.time.LocalDate;

public record AsignacionEnfermerosDTO(
    Long idAsignacion,
    Long idEnfermero,
    Long idHospitalizacion,
    LocalDate fechaAsignacion,
    Long idTurno,
    Boolean activo
) {}
