package com.example.hospital.citas.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
public record CitasDTO(
    Long idCita,
    java.time.LocalDate fecha,
    java.time.LocalTime hora,
    String motivo,
    java.time.LocalDateTime fechaCreacion,
    Long idEstado,
    Long idMedico,
    Long idPaciente,
    Boolean activo
) {}
