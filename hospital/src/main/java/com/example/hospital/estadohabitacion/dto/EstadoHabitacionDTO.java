package com.example.hospital.estadohabitacion.dto;

public record EstadoHabitacionDTO(
    Long idEstadoHabitacion,
    String nombre,
    String descripcion,
    Boolean activo
) {}
