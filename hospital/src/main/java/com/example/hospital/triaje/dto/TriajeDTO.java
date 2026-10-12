package com.example.hospital.triaje.dto;

public record TriajeDTO(
    Long idTriaje,
    String nivelDeEmergencia,
    String descripcion,
    Boolean activo
) {}
