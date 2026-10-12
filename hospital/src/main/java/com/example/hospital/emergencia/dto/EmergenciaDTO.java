package com.example.hospital.emergencia.dto;

import java.time.LocalDateTime;
public record EmergenciaDTO(
    Long idEmergencia,
    String nombreTemporal,
    LocalDateTime fechaHoraIngreso,
    String motivo,
    String signosVitales,
    LocalDateTime fechaHoraAlta,
    Long idPaciente,
    Long idHabitacion,
    Long idMedico,
    Long idTriaje,
    Boolean activo
) {}
