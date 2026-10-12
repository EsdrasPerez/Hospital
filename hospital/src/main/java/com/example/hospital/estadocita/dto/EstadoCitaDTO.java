package com.example.hospital.estadocita.dto;

public record EstadoCitaDTO(
    Long idEstado,
    String nombre,
    String descripcion,
    Boolean activo
) {}
