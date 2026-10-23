package com.example.hospital.hospitalizaciones.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * idEstadoHosp, fechaAlta y activo son de solo lectura: el estado lo fija el servidor (Activa al
 * ingresar, Alta con POST /{id}/alta) y la fecha de ingreso no se edita con PUT.
 */
public record HospitalizacionesDTO(
    Long idHospitalizacion,
    @NotNull(message = "El paciente es obligatorio")
    Long idPaciente,
    @NotNull(message = "La habitación es obligatoria")
    Long idHabitacion,
    @NotNull(message = "El médico responsable es obligatorio")
    Long idMedicoResponsable,
    Long idClinica,
    LocalDateTime fechaIngreso,
    LocalDateTime fechaAlta,
    @NotBlank(message = "El motivo de ingreso es obligatorio")
    String motivoIngreso,
    String diagnosticoIngreso,
    Long idEstadoHosp,
    Boolean activo
) {}
