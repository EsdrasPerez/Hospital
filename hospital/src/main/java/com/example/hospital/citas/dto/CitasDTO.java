package com.example.hospital.citas.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record CitasDTO(
    Long idCita,
    @NotNull(message = "La fecha de la cita es obligatoria")
    LocalDate fecha,
    @NotNull(message = "La hora de la cita es obligatoria")
    LocalTime hora,
    @Size(max = 255, message = "El motivo no puede superar 255 caracteres")
    String motivo,
    LocalDateTime fechaCreacion,
    @NotNull(message = "El estado de la cita es obligatorio")
    Long idEstado,
    @NotNull(message = "El médico es obligatorio")
    Long idMedico,
    @NotNull(message = "El paciente es obligatorio")
    Long idPaciente,
    Boolean activo
) {}
