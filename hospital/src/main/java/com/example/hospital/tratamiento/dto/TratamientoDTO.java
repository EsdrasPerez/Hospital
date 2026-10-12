package com.example.hospital.tratamiento.dto;

import java.time.LocalDate;

public record TratamientoDTO(
    Long idTratamiento,
    String descripcion,
    Integer duracionDias,
    String indicaciones,
    LocalDate fechaInicio,
    LocalDate fechaFin,
    Long idDiagnostico,
    Boolean activo
) {}
