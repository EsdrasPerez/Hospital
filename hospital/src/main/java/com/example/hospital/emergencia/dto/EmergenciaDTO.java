package com.example.hospital.emergencia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * fechaHoraAlta es solo de lectura: el alta se registra únicamente con PATCH /{id}/dar-alta
 * (en POST y PUT se ignora).
 */
public record EmergenciaDTO(
    Long idEmergencia,
    @Size(max = 150, message = "El nombre temporal no puede superar 150 caracteres")
    String nombreTemporal,
    LocalDateTime fechaHoraIngreso,
    @NotBlank(message = "El motivo de atención de la emergencia es obligatorio")
    String motivo,
    String signosVitales,
    LocalDateTime fechaHoraAlta,
    Long idPaciente,
    Long idHabitacion,
    Long idMedico,
    @NotNull(message = "El nivel de triaje es obligatorio")
    Long idTriaje,
    Boolean activo
) {}
