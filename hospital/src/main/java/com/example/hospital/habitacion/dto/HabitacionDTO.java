package com.example.hospital.habitacion.dto;

public record HabitacionDTO(
    Long idHabitacion,
    String numero,
    Integer piso,
    Integer capacidad,
    Long idTipoHabitacion,
    Long idEstadoHabitacion,
    Boolean activo
) {}
