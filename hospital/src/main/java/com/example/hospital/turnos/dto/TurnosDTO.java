package com.example.hospital.turnos.dto;

import java.time.LocalTime;
public record TurnosDTO(
    Long idTurno,
    String descripcion,
    java.time.LocalTime horaInicio,
    java.time.LocalTime horaFin,
    Boolean activo
) {}
