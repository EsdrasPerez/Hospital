package com.example.hospital.tiposdiagnostico.dto;

public record TiposDiagnosticoDTO(
    Long idTipoDiagnostico,
    String descripcion,
    Boolean activo
) {}
