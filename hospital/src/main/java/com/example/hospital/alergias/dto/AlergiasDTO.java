package com.example.hospital.alergias.dto;

public record AlergiasDTO(
    Long idAlergias,
    String nombre,
    String descripcion,
    Boolean activo
) {}
