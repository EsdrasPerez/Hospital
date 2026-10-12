package com.example.hospital.diagnosticos.dto;

import java.time.LocalDate;
public record DiagnosticosDTO(
    Long idDiagnostico,
    String observaciones,
    Long idTipoDiagnostico,
    String descripcion,
    java.time.LocalDate fecha,
    Boolean activo
) {}
