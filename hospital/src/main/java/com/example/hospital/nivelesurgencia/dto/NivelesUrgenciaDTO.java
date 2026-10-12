package com.example.hospital.nivelesurgencia.dto;

public record NivelesUrgenciaDTO(
    Long idNivelUrgencia,
    String codigo,
    String descripcion,
    Integer tiempoMaxEsperaMin,
    String color,
    Boolean activo
) {}
