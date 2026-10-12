package com.example.hospital.hospitalizaciones.dto;

import java.time.LocalDateTime;

public record HospitalizacionesDTO(
    Long idHospitalizacion,
    Long idPaciente,
    Long idHabitacion,
    Long idMedicoResponsable,
    Long idClinica,
    LocalDateTime fechaIngreso,
    LocalDateTime fechaAlta,
    String motivoIngreso,
    String diagnosticoIngreso,
    Long idEstadoHosp,
    Boolean activo
) {}
