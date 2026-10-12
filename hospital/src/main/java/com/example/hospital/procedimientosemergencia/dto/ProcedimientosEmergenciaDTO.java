package com.example.hospital.procedimientosemergencia.dto;

import java.time.LocalDateTime;
public record ProcedimientosEmergenciaDTO(
    Long idProcedimiento,
    Long idEmergencia,
    String descripcion,
    java.time.LocalDateTime fechaHora,
    Long idMedico,
    String observaciones,
    Boolean activo
) {}
