package com.example.hospital.estadoshospitalizacion.dto;

public record EstadosHospitalizacionDTO(
    Long idEstadoHosp,
    String descripcion,
    Boolean activo
) {}
